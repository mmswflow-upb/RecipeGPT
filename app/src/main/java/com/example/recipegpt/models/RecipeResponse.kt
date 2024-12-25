package com.example.recipegpt.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
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
    val servings: Int
) : Parcelable

@Parcelize
data class Ingredient(
    @SerializedName("item") val item: String,
    @SerializedName("amount") val amount: Number,
    @SerializedName("unit") val unit: String
) : Parcelable

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






