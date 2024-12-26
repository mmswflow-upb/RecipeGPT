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
    milligrams("milligrams"),
    kilograms("kilograms"),
    liters("liters"),
    milliliters("milliliters"),
    cups("cups"),
    tablespoons("tablespoons"),
    teaspoons("teaspoons"),
    tablespoons_solids_plants_powders("tablespoons_solids_plants_powders"),
    teaspoons_solids_plants_powders("teaspoons_solids_plants_powders"),
    whole_pieces("whole_pieces"),
    piece_about_50_grams("piece_about_50_grams"),
    piece_about_100_grams("piece_about_100_grams"),
    piece_about_250_grams("piece_about_250_grams"),

    drops("drops")
}

object UnitConverter {

    private val conversionRates = mapOf(
        // Mass conversions
        Pair(QuantUnit.milligrams, QuantUnit.grams) to 0.001,
        Pair(QuantUnit.grams, QuantUnit.milligrams) to 1000.0,
        Pair(QuantUnit.grams, QuantUnit.kilograms) to 0.001,
        Pair(QuantUnit.kilograms, QuantUnit.grams) to 1000.0,
        Pair(QuantUnit.milligrams, QuantUnit.kilograms) to 0.000001,
        Pair(QuantUnit.kilograms, QuantUnit.milligrams) to 1000000.0,

        // Volume conversions
        Pair(QuantUnit.milliliters, QuantUnit.liters) to 0.001,
        Pair(QuantUnit.liters, QuantUnit.milliliters) to 1000.0,
        Pair(QuantUnit.tablespoons, QuantUnit.teaspoons) to 3.0,
        Pair(QuantUnit.teaspoons, QuantUnit.tablespoons) to 1.0 / 3.0,
        Pair(QuantUnit.tablespoons, QuantUnit.cups) to 1.0 / 16.0,
        Pair(QuantUnit.cups, QuantUnit.tablespoons) to 16.0,
        Pair(QuantUnit.milliliters, QuantUnit.teaspoons) to 4.93,
        Pair(QuantUnit.teaspoons, QuantUnit.milliliters) to 1.0 / 4.93,
        Pair(QuantUnit.milliliters, QuantUnit.tablespoons) to 14.79,
        Pair(QuantUnit.tablespoons, QuantUnit.milliliters) to 1.0 / 14.79,
        Pair(QuantUnit.milliliters, QuantUnit.cups) to 240.0,
        Pair(QuantUnit.cups, QuantUnit.milliliters) to 1.0 / 240.0,

        // New units for solids (tablespoons and teaspoons)
        Pair(QuantUnit.tablespoons_solids_plants_powders, QuantUnit.teaspoons_solids_plants_powders) to 3.0,
        Pair(QuantUnit.teaspoons_solids_plants_powders, QuantUnit.tablespoons_solids_plants_powders) to 1.0 / 3.0,
        Pair(QuantUnit.tablespoons_solids_plants_powders, QuantUnit.grams) to 12.5,
        Pair(QuantUnit.grams, QuantUnit.tablespoons_solids_plants_powders) to 1.0 / 12.5,
        Pair(QuantUnit.teaspoons_solids_plants_powders, QuantUnit.grams) to 4.2,
        Pair(QuantUnit.grams, QuantUnit.teaspoons_solids_plants_powders) to 1.0 / 4.2,

        // Discrete units
        Pair(QuantUnit.whole_pieces, QuantUnit.whole_pieces) to 1.0, // No conversion needed
        Pair(QuantUnit.piece_about_50_grams, QuantUnit.grams) to 50.0,
        Pair(QuantUnit.piece_about_100_grams, QuantUnit.grams) to 100.0,
        Pair(QuantUnit.piece_about_250_grams, QuantUnit.grams) to 250.0,
        Pair(QuantUnit.grams, QuantUnit.piece_about_50_grams) to 1.0 / 50.0,
        Pair(QuantUnit.grams, QuantUnit.piece_about_100_grams) to 1.0 / 100.0,
        Pair(QuantUnit.grams, QuantUnit.piece_about_250_grams) to 1.0 / 250.0
    )

    fun convert(value: Double, from: QuantUnit, to: QuantUnit): Double {
        return if (from == to) {
            value
        } else {
            conversionRates[Pair(from, to)]?.let { value * it }
                ?: throw IllegalArgumentException("Conversion from $from to $to is not supported.")
        }
    }

    fun add(value1: Double, unit1: QuantUnit, value2: Double, unit2: QuantUnit): Pair<Double, QuantUnit> {
        return try {
            // Convert both values to a compatible unit (prefer the larger unit for normalization)
            val compatibleUnit = findCompatibleUnit(unit1, unit2) ?: throw IllegalArgumentException("Units are not compatible")
            val normalizedValue1 = convert(value1, unit1, compatibleUnit)
            val normalizedValue2 = convert(value2, unit2, compatibleUnit)

            // Add the normalized values
            val total = normalizedValue1 + normalizedValue2

            // Find the optimal unit for the combined value
            val optimalUnit = getOptimalUnit(total, compatibleUnit)
            val normalizedTotal = convert(total, compatibleUnit, optimalUnit)

            Pair(normalizedTotal, optimalUnit)
        } catch (e: IllegalArgumentException) {
            // Handle incompatible units by keeping the first value and unit unchanged
            Pair(value1, unit1)
        }
    }

    // Helper function to find a compatible unit between two units
    private fun findCompatibleUnit(unit1: QuantUnit, unit2: QuantUnit): QuantUnit? {
        return when {
            unit1 == unit2 -> unit1
            conversionRates.containsKey(Pair(unit1, unit2)) -> unit2
            conversionRates.containsKey(Pair(unit2, unit1)) -> unit1
            else -> null
        }
    }

    fun getOptimalUnit(amount: Double, currentUnit: QuantUnit): QuantUnit {
        return when (currentUnit) {
            QuantUnit.milligrams -> when {
                amount >= 1000000 -> QuantUnit.kilograms
                amount >= 1000 -> QuantUnit.grams
                else -> QuantUnit.milligrams
            }
            QuantUnit.grams -> when {
                amount >= 1000 -> QuantUnit.kilograms
                amount < 50 -> QuantUnit.piece_about_50_grams
                else -> QuantUnit.grams
            }
            QuantUnit.kilograms -> QuantUnit.kilograms
            QuantUnit.milliliters -> if (amount >= 1000) QuantUnit.liters else QuantUnit.milliliters
            QuantUnit.liters -> QuantUnit.liters
            QuantUnit.tablespoons -> if (amount >= 16) QuantUnit.cups else QuantUnit.tablespoons
            QuantUnit.teaspoons -> if (amount >= 3) QuantUnit.tablespoons else QuantUnit.teaspoons
            QuantUnit.tablespoons_solids_plants_powders -> if (amount >= 16) QuantUnit.cups else QuantUnit.tablespoons_solids_plants_powders
            QuantUnit.teaspoons_solids_plants_powders -> if (amount >= 3) QuantUnit.tablespoons_solids_plants_powders else QuantUnit.teaspoons_solids_plants_powders
            QuantUnit.cups -> QuantUnit.cups
            QuantUnit.whole_pieces, QuantUnit.piece_about_50_grams, QuantUnit.piece_about_100_grams, QuantUnit.piece_about_250_grams -> currentUnit
            QuantUnit.drops -> QuantUnit.drops
        }
    }
}








