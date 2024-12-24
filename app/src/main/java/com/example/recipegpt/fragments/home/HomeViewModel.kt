package com.example.recipegpt.fragments.home

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.recipegpt.models.Recipe
import com.example.recipegpt.services.RecipeService
import com.example.recipegpt.services.SearchNotificationForegroundService
import com.example.recipegpt.workers.RandomQuoteWorker
import java.util.concurrent.TimeUnit

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // Recipe Service and Binding State
    @SuppressLint("StaticFieldLeak")
    private var _recipeService: RecipeService? = null
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



    // Service connection for RecipeService
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RecipeService.LocalBinder
            _recipeService = binder.getService()
            _isBound.value = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            _recipeService = null
            _isBound.value = false
        }
    }

    init {
        // Schedule the worker when ViewModel is initialized
        scheduleQuoteWorker()
    }

    fun bindService(context: Context) {
        val intent = Intent(context, RecipeService::class.java)
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

    fun updateSearchingStatus(status: Boolean) {
        _isSearching.value = status
    }

    private fun updateRecipes(newRecipes: List<Recipe>) {
        _recipes.value = newRecipes
    }



    private fun scheduleQuoteWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<RandomQuoteWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(getApplication()).enqueueUniquePeriodicWork(
            "RandomQuoteWorker",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun startSearchNotificationService(context: Context, query: String) {
        Log.d("HomeViewModel-startSearchNotificationService", "Passed Query: $query")
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



    fun searchRecipes(query: String) {

        val context = getApplication<Application>().applicationContext

        if (_isBound.value == true && _recipeService != null) {
            updateSearchingStatus(true)

            // Start the foreground service

            startSearchNotificationService(context, query)

            _recipeService?.searchRecipes(query) { recipes ->
                updateRecipes(recipes)
                updateSearchingStatus(false)

                // Stop the foreground service
                stopSearchNotificationService(context)
            }
        }
    }



}
