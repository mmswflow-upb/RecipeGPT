package com.example.recipegpt.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.recipegpt.data.converters.Converters
import com.example.recipegpt.models.Ingredient

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val estimatedCookingTime: Int,
    val servings: Int,
    @TypeConverters(Converters::class) val ingredients: List<Ingredient>,
    @TypeConverters(Converters::class) val instructions: List<String>
)




