package com.example.recipegpt.services

import android.app.Service
import android.content.Intent
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
import java.net.SocketTimeoutException

class RecipeService : Service() {

    private lateinit var recipeDao: RecipeDao
    private var apiService: ApiInterface? = null // Use nullable to prevent crashes

    override fun onCreate() {
        super.onCreate()
        // Initialize DAO and API Service
        recipeDao = AppDatabase.getInstance(applicationContext).recipeDao()
        apiService = ApiClient.instance.create(ApiInterface::class.java)
        Log.d("RecipeService", "Initialized apiService")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val query = intent?.getStringExtra("query") ?: "default query"

        CoroutineScope(Dispatchers.IO).launch {
            apiService?.let {
                try {
                    val recipes = searchRecipes(query)

                    Log.d("RecipeService", "Fetched ${recipes.size} recipes")

                } catch (e: Exception) {
                    Log.e("RecipeService", "Error fetching recipes: ${e.message}")
                } finally {
                    stopSelf() // Stop the service after work is completed
                }
            } ?: run {
                Log.e("RecipeService", "API service not initialized")
                stopSelf() // Stop the service if initialization failed
            }
        }
        return START_NOT_STICKY
    }

    /**
     * Search recipes from the API based on a query.
     */
    suspend fun searchRecipes(query: String): List<Recipe> = withContext(Dispatchers.IO) {
        apiService?.let {
            try {
                val response = it.searchRecipes(1, query).execute()
                Log.d("Raw JSON Response", response.body().toString())
                if (response.isSuccessful) {
                    val recipesResponse = response.body()
                    Log.d("ResponseBody", (recipesResponse ?: "No response body").toString())

                    Log.d("RecipeService", "Fetched ${recipesResponse?.recipes?.size} recipes")
                    recipesResponse?.recipes ?: emptyList()
                } else {
                    Log.e("RecipeService", "Error: ${response.code()} - ${response.message()}")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("RecipeService", "Error fetching recipes: ${e.message}")
                emptyList()
            }
        } ?: emptyList()
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
