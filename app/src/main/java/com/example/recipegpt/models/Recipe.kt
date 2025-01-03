package com.example.recipegpt.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class RecipesResponse(
    val recipes: List<Recipe>
)

@Parcelize
data class Recipe(
    val title: String,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
    val estimatedCookingTime: Int,
    val servings: Int,
    var listed: Boolean = false
) : Parcelable






