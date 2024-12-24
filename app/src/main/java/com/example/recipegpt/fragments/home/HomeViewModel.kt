package com.example.recipegpt.viewmodels

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.recipegpt.models.Recipe
import com.example.recipegpt.services.RecipeService
import com.example.recipegpt.workers.RandomQuoteWorker
import java.util.concurrent.TimeUnit

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // Recipe Service and Binding State
    private var _recipeService: RecipeService? = null
    private val _isBound = MutableLiveData<Boolean>(false)
    val isBound: LiveData<Boolean> get() = _isBound

    // LiveData for query text
    private val _query = MutableLiveData<String>()
    val query: LiveData<String> get() = _query

    // LiveData for searching status
    private val _isSearching = MutableLiveData<Boolean>(false)
    val isSearching: LiveData<Boolean> get() = _isSearching

    // LiveData for API results
    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> get() = _recipes

    // LiveData for current page (optional for pagination)
    private val _currentPage = MutableLiveData<Int>(1)
    val currentPage: LiveData<Int> get() = _currentPage

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

    fun updateRecipes(newRecipes: List<Recipe>) {
        _recipes.value = newRecipes
    }

    fun updateCurrentPage(page: Int) {
        _currentPage.value = page
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

    fun searchRecipes(query: String, onResult: (List<Recipe>) -> Unit) {
        if (_isBound.value == true && _recipeService != null) {
            _recipeService?.searchRecipes(query, onResult)
        }
    }
}
