package com.example.recipegpt.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.recipegpt.models.GsonConverters
import com.example.recipegpt.models.Ingredient
import com.example.recipegpt.models.Recipe

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val title: String,
    val estimatedCookingTime: Int,
    val servings: Int,
    var listed: Boolean,
    @TypeConverters(GsonConverters::class) val ingredients: List<Ingredient>,
    @TypeConverters(GsonConverters::class) val instructions: List<String>
)


fun Recipe.toRecipeEntity() = RecipeEntity(
    title = this.title,
    estimatedCookingTime = this.estimatedCookingTime,
    servings = this.servings,
    ingredients = this.ingredients,
    instructions = this.instructions,
    listed= this.listed
)

fun RecipeEntity.toRecipeModel() = Recipe(
    title = this.title,
    estimatedCookingTime = this.estimatedCookingTime,
    servings = this.servings,
    ingredients = this.ingredients,
    instructions = this.instructions,
    listed= this.listed

)




