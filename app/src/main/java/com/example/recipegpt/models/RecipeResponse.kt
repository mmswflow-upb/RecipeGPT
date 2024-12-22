package com.example.recipegpt.models

data class RecipeResponse(
    val recipes: List<Recipe>
)

data class Recipe(
    val title: String,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
    val estimatedCookingTime: Int,
    val servings: Int
)


