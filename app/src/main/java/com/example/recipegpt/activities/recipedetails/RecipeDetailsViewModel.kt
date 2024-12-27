package com.example.recipegpt.activities.recipedetails

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.recipegpt.models.Ingredient
import com.example.recipegpt.models.Recipe
import com.example.recipegpt.services.DatabaseBackgroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecipeDetailsViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    // LiveData to hold the current recipe
    private val _recipe = MutableLiveData<Recipe>()
    val recipe: LiveData<Recipe> get() = _recipe

    // LiveData for saved status
    private val _isRecipeSaved = MutableLiveData(false)
    val isRecipeSaved: LiveData<Boolean> get() = _isRecipeSaved

    // LiveData for ingredient availability
    private val _ingredientAvailability = MutableLiveData<Map<String, Boolean>>()
    val ingredientAvailability: LiveData<Map<String, Boolean>> get() = _ingredientAvailability

    // Function to set the current recipe and fetch its saved status and ingredient availability
    fun setRecipe(newRecipe: Recipe) {
        _recipe.value = newRecipe
        checkIfRecipeIsSaved(newRecipe.title)
        checkIngredientAvailability(newRecipe.ingredients)
    }

    // Check if the recipe is saved in the database
    private fun checkIfRecipeIsSaved(recipeTitle: String) {
        val resultReceiver = object : android.os.ResultReceiver(null) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                val recipeExists = resultData?.getParcelable("data", Recipe::class.java) != null
                _isRecipeSaved.postValue(recipeExists)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val intent = Intent(context, DatabaseBackgroundService::class.java).apply {
                action = "GET_RECIPE_BY_NAME"
                putExtra("name", recipeTitle)
                putExtra("resultReceiver", resultReceiver)
            }
            context.startService(intent)
        }
    }

    init {

    }


    fun toggleListingStatus() {
        val currentRecipe = _recipe.value ?: return
        val updatedRecipe = currentRecipe.copy(listed = !currentRecipe.listed)

        // Update the recipe in the database
        CoroutineScope(Dispatchers.IO).launch {
            val intent = Intent(context, DatabaseBackgroundService::class.java).apply {
                action = "SAVE_OR_REPLACE_RECIPE"
                putExtra("recipe", updatedRecipe)
            }
            context.startService(intent)
        }

        // Update LiveData
        _recipe.postValue(updatedRecipe)
    }

    fun cookRecipe(callback: (Boolean) -> Unit) {
        val currentRecipe = _recipe.value ?: return

        val resultReceiver = object : android.os.ResultReceiver(null) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                val success = resultData?.getBoolean("success", false) ?: false
                callback(success)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val intent = Intent(context, DatabaseBackgroundService::class.java).apply {
                action = "COOK_RECIPE"
                putExtra("recipe", currentRecipe)
                putExtra("resultReceiver", resultReceiver)
            }
            context.startService(intent)
        }
    }


    // Save or delete the recipe
    fun toggleRecipeSavedStatus() {
        val currentRecipe = _recipe.value ?: return
        val isSaved = _isRecipeSaved.value ?: false

        val action = if (isSaved) "DELETE_RECIPE_BY_NAME" else "SAVE_OR_REPLACE_RECIPE"
        val intent = Intent(context, DatabaseBackgroundService::class.java).apply {
            this.action = action
            putExtra("name", currentRecipe.title)
            if (!isSaved) putExtra("recipe", currentRecipe) // Add recipe if saving
        }

        context.startService(intent)
        _isRecipeSaved.postValue(!isSaved) // Update UI state optimistically
    }



    // Check ingredient availability against saved ingredients
    private fun checkIngredientAvailability(requiredIngredients: List<Ingredient>) {
        val resultReceiver = object : android.os.ResultReceiver(null) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                val savedIngredients =
                    resultData?.getParcelableArrayList("data", Ingredient::class.java) ?: emptyList<Ingredient>()
                val availability = requiredIngredients.associate { requiredIngredient ->
                    val savedIngredient = savedIngredients.find { it.item.equals(requiredIngredient.item, ignoreCase = true) }
                    val isAvailable =
                        savedIngredient != null && savedIngredient.amount.toDouble() >= requiredIngredient.amount.toDouble()
                    Log.d("checkIngredientAvailability", "${requiredIngredient.item} available: $isAvailable")
                    requiredIngredient.item to isAvailable
                }
                _ingredientAvailability.postValue(availability)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val intent = Intent(context, DatabaseBackgroundService::class.java).apply {
                action = "GET_SAVED_INGREDIENTS"
                putExtra("resultReceiver", resultReceiver)
            }
            context.startService(intent)
        }
    }
}
