package com.example.recipegpt.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.recipegpt.data.AppDatabase
import com.example.recipegpt.data.dao.RecipeDao
import com.example.recipegpt.data.entities.RecipeEntity
import com.example.recipegpt.models.Recipe
import com.example.recipegpt.network.ApiClient
import com.example.recipegpt.network.ApiInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeService : Service() {

    private lateinit var recipeDao: RecipeDao
    private var apiService: ApiInterface? = null

    // Binder instance to bind the service
    private val binder = LocalBinder()

    // Inner class for the client Binder
    inner class LocalBinder : Binder() {
        fun getService(): RecipeService = this@RecipeService
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize DAO and API Service
        recipeDao = AppDatabase.getInstance(applicationContext).recipeDao()
        apiService = ApiClient.instance.create(ApiInterface::class.java)
        Log.d("RecipeService", "Initialized apiService")
    }

    /**
     * Perform a search for recipes based on the given query.
     * This method can be invoked from an activity or other components.
     */
    fun searchRecipes(query: String, callback: (List<Recipe>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val recipes = apiService?.let {
                try {
                    val response = it.searchRecipes(2, query).execute()
                    if (response.isSuccessful) {
                        response.body()?.recipes ?: emptyList()
                    } else {
                        Log.e("RecipeService", "Error: ${response.code()} - ${response.message()}")
                        emptyList()
                    }
                } catch (e: Exception) {
                    Log.e("RecipeService", "Error fetching recipes: ${e.message}")
                    emptyList()
                }
            } ?: emptyList()

            withContext(Dispatchers.Main) {
                callback(recipes)
            }
        }
    }

    /**
     * Save a single recipe to the Room database.
     */
    fun saveRecipe(recipe: Recipe) {
        CoroutineScope(Dispatchers.IO).launch {
            val recipeEntity = RecipeEntity(
                id = generateRecipeId(), // Generate or pass a unique ID
                title = recipe.title,
                estimatedCookingTime = recipe.estimatedCookingTime,
                servings = recipe.servings,
                ingredients = recipe.ingredients,
                instructions = recipe.instructions
            )
            recipeDao.insertRecipe(recipeEntity)
        }
    }

    // Helper function to generate a unique ID (if not already provided)
    private fun generateRecipeId(): Int {
        return (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}
