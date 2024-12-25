package com.example.recipegpt.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.recipegpt.models.Converters
import com.example.recipegpt.models.Ingredient
import com.example.recipegpt.models.Recipe

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val estimatedCookingTime: Int,
    val servings: Int,
    @TypeConverters(Converters::class) val ingredients: List<Ingredient>,
    @TypeConverters(Converters::class) val instructions: List<String>
)


fun Recipe.toRecipeEntity() = RecipeEntity(
    id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(), // Generate a unique ID
    title = this.title,
    estimatedCookingTime = this.estimatedCookingTime,
    servings = this.servings,
    ingredients = this.ingredients,
    instructions = this.instructions
)

fun RecipeEntity.toRecipeModel() = Recipe(
    title = this.title,
    estimatedCookingTime = this.estimatedCookingTime,
    servings = this.servings,
    ingredients = this.ingredients,
    instructions = this.instructions
)




