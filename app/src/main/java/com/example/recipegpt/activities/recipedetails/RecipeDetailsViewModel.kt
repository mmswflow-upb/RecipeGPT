package com.example.recipegpt.activities.recipedetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.recipegpt.models.Recipe

class RecipeDetailsViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData to hold the current recipe
    private val _recipe = MutableLiveData<Recipe>()
    val recipe: LiveData<Recipe> get() = _recipe

    // Function to set the current recipe
    fun setRecipe(newRecipe: Recipe) {
        _recipe.value = newRecipe
    }
}
