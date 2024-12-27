
package com.example.recipegpt.data.entities

import androidx.room.Entity
import androidx.room.TypeConverters
import com.example.recipegpt.models.Converters
import com.example.recipegpt.models.Ingredient

//Primary key is a combo of the item and unit
@Entity(tableName = "ingredients", primaryKeys = ["item", "unit"])
data class IngredientEntity(
    val item: String,
    @TypeConverters(Converters::class) val amount: Double, // Amount of the ingredient
    @TypeConverters(Converters::class) val unit: String // Measurement unit (e.g., grams, cups)
)

fun Ingredient.toIngredientEntity() = IngredientEntity(
    item = this.item.lowercase(), // Convert to lowercase for case-insensitivity
    amount = this.amount.toDouble(),
    unit = this.unit
)

fun IngredientEntity.toIngredientModel() = Ingredient(
    item = this.item,
    amount = this.amount,
    unit = this.unit
)