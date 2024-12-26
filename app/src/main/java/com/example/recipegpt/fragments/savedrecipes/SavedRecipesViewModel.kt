package com.example.recipegpt.fragments.savedrecipes

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.recipegpt.models.Recipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SavedRecipesViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    // LiveData to hold all saved recipes (complete list)
    private val _savedRecipes = MutableLiveData<List<Recipe>>()

    // LiveData to hold the filtered recipes
    private val _filteredRecipes = MutableLiveData<List<Recipe>>()
    val filteredRecipes: LiveData<List<Recipe>> get() = _filteredRecipes

    // LiveData to hold the query in the search bar
    private val _query = MutableLiveData("")
    val query: LiveData<String> get() = _query


    // BroadcastReceiver to listen for database changes
    private val savedRecipesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.recipegpt.LOCAL_DB_CHANGED") {
                fetchSavedRecipes()
            }
        }
    }

    init {
        // Register BroadcastReceiver
        val intentFilter = IntentFilter("com.example.recipegpt.LOCAL_DB_CHANGED")
        LocalBroadcastManager.getInstance(context).registerReceiver(savedRecipesReceiver, intentFilter)

        // Fetch saved recipes on initialization
        fetchSavedRecipes()
        filterRecipes(_query.value!!)
    }

    private fun fetchSavedRecipes() {
        CoroutineScope(Dispatchers.IO).launch {
            val intent = Intent(context, com.example.recipegpt.services.DatabaseBackgroundService::class.java).apply {
                action = "GET_SAVED_RECIPES"
                putExtra("resultReceiver", object : android.os.ResultReceiver(null) {
                    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                        val recipes: List<Recipe>? =
                            resultData?.getParcelableArrayList("data", Recipe::class.java)
                        _savedRecipes.postValue(recipes ?: emptyList())
                        // Initially set the filtered list to the complete list
                        _filteredRecipes.postValue(recipes ?: emptyList())
                    }
                })
            }
            context.startService(intent)
        }
    }

    // Function to filter recipes based on a query
    fun filterRecipes(query: String) {
        val filteredList = _savedRecipes.value?.filter { recipe ->
            recipe.title.contains(query, ignoreCase = true)
        }
        _filteredRecipes.postValue(filteredList ?: emptyList())
    }

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
        filterRecipes(newQuery)
    }

    override fun onCleared() {
        super.onCleared()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(savedRecipesReceiver) // Unregister receiver
    }
}

