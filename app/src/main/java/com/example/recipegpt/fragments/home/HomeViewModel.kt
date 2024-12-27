package com.example.recipegpt.fragments.home

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.recipegpt.models.QuantUnit
import com.example.recipegpt.models.Recipe
import com.example.recipegpt.services.GenerateRecipeService
import com.example.recipegpt.services.SearchNotificationForegroundService
import com.example.recipegpt.utils.SharedPreferencesManager
import com.example.recipegpt.workers.RandomQuoteWorker
import java.util.concurrent.TimeUnit

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // Recipe Service and Binding State
    @SuppressLint("StaticFieldLeak")
    private var _recipeService: GenerateRecipeService? = null
    private val _isBound = MutableLiveData(false)

    // LiveData for query text
    private val _query = MutableLiveData<String>()
    val query: LiveData<String> get() = _query

    // LiveData for searching status
    private val _isSearching = MutableLiveData(false)
    val isSearching: LiveData<Boolean> get() = _isSearching

    // LiveData for API results
    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> get() = _recipes

    // SharedPreferencesManager
    private val sharedPreferencesManager = SharedPreferencesManager(application)

    // LiveData for settings
    private val _numberOfRecipes = MutableLiveData(sharedPreferencesManager.getMaxResults())

    private val _randomQuoteFrequency = MutableLiveData(sharedPreferencesManager.getRandomQuoteFrequency())

    // Service connection for RecipeService
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as GenerateRecipeService.LocalBinder
            _recipeService = binder.getService()
            _isBound.value = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            _recipeService = null
            _isBound.value = false
        }
    }

    private val preferencesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val newFrequency = intent.getStringExtra(SharedPreferencesManager.EXTRA_RANDOM_QUOTE_FREQUENCY)
            val newMaxResults = intent.getIntExtra(SharedPreferencesManager.EXTRA_MAX_RESULTS, _numberOfRecipes.value ?: 2)

            _randomQuoteFrequency.value = newFrequency
            _numberOfRecipes.value = newMaxResults

            // Update the WorkManager with the new frequency
            newFrequency?.let { scheduleQuoteWorker(it) }
        }
    }

    init {
        Log.d("HomeViewModel-initialization", "Initializing home view model")
        // Schedule the worker with the initial settings
        scheduleQuoteWorker(_randomQuoteFrequency.value ?: "15 minutes")

        // Listen for shared preferences updates
        val filter = IntentFilter(SharedPreferencesManager.ACTION_SETTINGS_UPDATED)
        LocalBroadcastManager.getInstance(application).registerReceiver(preferencesReceiver, filter)
    }

    fun bindService(context: Context) {
        val intent = Intent(context, GenerateRecipeService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService(context: Context) {
        if (_isBound.value == true) {
            context.unbindService(serviceConnection)
            _isBound.value = false
        }
    }

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun updateGeneratingStatus(status: Boolean) {
        _isSearching.value = status
    }

    private fun updateRecipes(newRecipes: List<Recipe>) {
        _recipes.value = newRecipes
    }

    private fun scheduleQuoteWorker(frequency: String) {
        val intervalMinutes = when (frequency) {
            "15 minutes" -> 15L
            "1 hour" -> 60L
            "3 hours" -> 180L
            "Once per day" -> 1440L
            else -> 0L // Never
        }
        Log.d("HomeViewModel", "Scheduling random quotes worker")
        if (intervalMinutes > 0) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<RandomQuoteWorker>(intervalMinutes, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(getApplication()).enqueueUniquePeriodicWork(
                "RandomQuoteWorker",
                androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        } else {
            WorkManager.getInstance(getApplication()).cancelUniqueWork("RandomQuoteWorker")
        }
    }

    private fun startSearchNotificationService(context: Context, query: String) {
        val intent = Intent(context, SearchNotificationForegroundService::class.java).apply {
            action = "START"
            putExtra("query", query)
        }
        context.startForegroundService(intent)
    }

    private fun stopSearchNotificationService(context: Context) {
        val intent = Intent(context, SearchNotificationForegroundService::class.java).apply {
            action = "STOP"
        }
        context.startService(intent)
    }

    fun generateRecipes(query: String, disableEditText: (Boolean) -> Unit) {
        val context = getApplication<Application>().applicationContext

        if (_isBound.value == true && _recipeService != null) {
            updateGeneratingStatus(true)

            // Disable the EditText
            disableEditText(true)

            // Start the foreground service
            startSearchNotificationService(context, query)

            _recipeService?.generateRecipes(query, _numberOfRecipes.value ?: 2) { recipes ->
                val updatedRecipes = recipes.map { recipe ->
                    recipe.copy(
                        ingredients = recipe.ingredients.map { ingredient ->
                            val unit = try {
                                QuantUnit.valueOf(ingredient.unit)
                                ingredient.unit // Valid unit
                            } catch (e: IllegalArgumentException) {
                                Log.d("HomeViewModel-generateRecipes", "${ingredient.item}: Unit didn't match enum, converting to whole_pieces")
                                QuantUnit.whole_pieces.unit // Replace with default
                            }

                            ingredient.copy(unit = unit)
                        }
                    )
                }

                updateRecipes(updatedRecipes)
                updateGeneratingStatus(false)

                // Re-enable the EditText
                disableEditText(false)

                // Stop the foreground service
                stopSearchNotificationService(context)
            }
        }
    }





    override fun onCleared() {
        super.onCleared()
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(preferencesReceiver)
    }
}
