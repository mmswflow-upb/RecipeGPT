package com.example.recipegpt.services

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.ResultReceiver
import android.util.Log
import com.example.recipegpt.data.AppDatabase
import com.example.recipegpt.data.entities.toIngredientEntity
import com.example.recipegpt.data.entities.toIngredientModel
import com.example.recipegpt.data.entities.toRecipeEntity
import com.example.recipegpt.data.entities.toRecipeModel
import com.example.recipegpt.models.Ingredient
import com.example.recipegpt.models.Recipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseBackgroundService : Service() {

    // Database instance
    private lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(applicationContext)
        Log.d("DBBackgroundService-onCreate", "database initialized")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val resultReceiver = intent?.getParcelableExtra("resultReceiver", ResultReceiver::class.java)

        Log.d("DBBackgroundService-onStartCommand", "The action: $action")
        when (action) {
            "GET_SAVED_RECIPES" -> getSavedRecipes(resultReceiver)
            "GET_RECIPE_BY_NAME" -> {
                val name = intent.getStringExtra("name")
                Log.d("DBBackgroundService-onStartCommand", "Searched recipe name: $name")

                if (name != null) getSavedRecipeByName(name, resultReceiver)
            }
            "SAVE_OR_REPLACE_RECIPE" -> {
                val recipe = intent.getParcelableExtra("recipe", Recipe::class.java)
                Log.d("DBBackgroundService-onStartCommand", "Saved Recipe name: ${recipe?.title}")

                if (recipe != null) saveOrReplaceRecipe(recipe, resultReceiver)
            }
            "DELETE_RECIPE_BY_NAME" -> {
                val name = intent.getStringExtra("name")
                Log.d("DBBackgroundService-onStartCommand", "Deleted Recipe name: $name")
                if (name != null) deleteRecipeByName(name, resultReceiver)
            }
            "GET_SAVED_INGREDIENTS" -> getSavedIngredients(resultReceiver)
            "GET_INGREDIENT_BY_NAME" -> {
                val name = intent.getStringExtra("name")
                if (name != null) getSavedIngredientByName(name, resultReceiver)
            }
            "SAVE_OR_UPDATE_INGREDIENT" -> {
                val ingredient = intent.getParcelableExtra("ingredient", Ingredient::class.java)
                if (ingredient != null) saveOrUpdateIngredient(ingredient, resultReceiver)
            }
            "DELETE_INGREDIENT_BY_NAME" -> {
                val name = intent.getStringExtra("name")
                if (name != null) deleteIngredientByName(name, resultReceiver)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun getSavedRecipes(resultReceiver: ResultReceiver?) {
        CoroutineScope(Dispatchers.IO).launch {
            val recipes = database.recipeDao().getAllRecipes()
            val recipeModels = recipes.value?.map { it.toRecipeModel() } ?: emptyList()
            val bundle = Bundle().apply {
                putParcelableArrayList("data", ArrayList(recipeModels))
            }
            resultReceiver?.send(0, bundle)
        }
    }

    private fun getSavedRecipeByName(name: String, resultReceiver: ResultReceiver?) {
        CoroutineScope(Dispatchers.IO).launch {
            val recipeEntity = database.recipeDao().getRecipeByNameSync(name)
            val recipe = recipeEntity?.toRecipeModel()
            val bundle = Bundle().apply {
                putParcelable("data", recipe)
            }
            resultReceiver?.send(0, bundle)
        }
    }

    private fun saveOrReplaceRecipe(recipe: Recipe, resultReceiver: ResultReceiver?) {
        CoroutineScope(Dispatchers.IO).launch {
            val recipeEntity = recipe.toRecipeEntity()
            val existingRecipe = database.recipeDao().getRecipeByNameSync(recipeEntity.title)
            if (existingRecipe != null) {
                database.recipeDao().updateRecipe(recipeEntity)
            } else {
                database.recipeDao().insertRecipe(recipeEntity)
            }
            resultReceiver?.send(0, null)
        }
    }

    private fun deleteRecipeByName(name: String, resultReceiver: ResultReceiver?) {
        CoroutineScope(Dispatchers.IO).launch {
            database.recipeDao().deleteRecipeByName(name)
            resultReceiver?.send(0, null)
        }
    }

    private fun getSavedIngredients(resultReceiver: ResultReceiver?) {
        CoroutineScope(Dispatchers.IO).launch {
            val ingredients = database.ingredientDao().getAllIngredients()
            val ingredientModels = ingredients.value?.map { it.toIngredientModel() } ?: emptyList()
            val bundle = Bundle().apply {
                putParcelableArrayList("data", ArrayList(ingredientModels))
            }
            resultReceiver?.send(0, bundle)
        }
    }

    private fun getSavedIngredientByName(name: String, resultReceiver: ResultReceiver?) {
        CoroutineScope(Dispatchers.IO).launch {
            val ingredientEntity = database.ingredientDao().getIngredientByNameSync(name)
            val ingredient = ingredientEntity?.toIngredientModel()
            val bundle = Bundle().apply {
                putParcelable("data", ingredient)
            }
            resultReceiver?.send(0, bundle)
        }
    }

    private fun saveOrUpdateIngredient(ingredient: Ingredient, resultReceiver: ResultReceiver?) {
        CoroutineScope(Dispatchers.IO).launch {
            val ingredientEntity = ingredient.toIngredientEntity()
            val existingIngredient = database.ingredientDao().getIngredientByNameSync(ingredientEntity.item)
            if (existingIngredient != null) {
                val updatedIngredient = existingIngredient.copy(
                    amount = existingIngredient.amount + ingredientEntity.amount
                )
                database.ingredientDao().updateIngredient(updatedIngredient)
            } else {
                database.ingredientDao().insertIngredient(ingredientEntity)
            }
            resultReceiver?.send(0, null)
        }
    }

    private fun deleteIngredientByName(name: String, resultReceiver: ResultReceiver?) {
        CoroutineScope(Dispatchers.IO).launch {
            database.ingredientDao().deleteIngredientByName(name)
            resultReceiver?.send(0, null)
        }
    }
}
