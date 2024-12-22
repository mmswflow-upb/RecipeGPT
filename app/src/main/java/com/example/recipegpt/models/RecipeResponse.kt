package com.example.recipegpt.models

import com.google.gson.annotations.SerializedName

data class RecipesResponse(
    val recipes: List<Recipe>
)

data class Recipe(
    val title: String,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
    val estimatedCookingTime: Int,
    val servings: Int
)

data class Ingredient(
    @SerializedName("item") val item: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("unit") val unit: String
)

enum class QuantUnit(val unit: String) {
    grams("grams"),
    kilograms("kilograms"),
    liters("liters"),
    milliliters("milliliters"),
    cups("cups"),
    tablespoons("tablespoons"),
    teaspoons("teaspoons"),
    pieces("pieces")
}






