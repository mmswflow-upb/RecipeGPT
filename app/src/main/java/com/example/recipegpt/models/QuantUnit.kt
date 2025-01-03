package com.example.recipegpt.models

import android.content.Context
import com.example.recipegpt.R

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

    fun getDisplayName(context: Context, unit: QuantUnit): String {
        return when (unit) {
            QuantUnit.grams -> context.getString(R.string.grams)
            QuantUnit.milligrams -> context.getString(R.string.milligrams)
            QuantUnit.kilograms -> context.getString(R.string.kilograms)
            QuantUnit.liters -> context.getString(R.string.liters)
            QuantUnit.milliliters -> context.getString(R.string.milliliters)
            QuantUnit.cups -> context.getString(R.string.cups)
            QuantUnit.tablespoons -> context.getString(R.string.tablespoons)
            QuantUnit.teaspoons -> context.getString(R.string.teaspoons)
            QuantUnit.tablespoons_solids_plants_powders -> context.getString(R.string.tablespoons_solid)
            QuantUnit.teaspoons_solids_plants_powders -> context.getString(R.string.teaspoons_solid)
            QuantUnit.whole_pieces -> context.getString(R.string.whole_pieces)
            QuantUnit.piece_about_50_grams -> context.getString(R.string.piece_about_50_grams)
            QuantUnit.piece_about_100_grams -> context.getString(R.string.piece_about_100_grams)
            QuantUnit.piece_about_250_grams -> context.getString(R.string.piece_about_250_grams)
            QuantUnit.drops -> context.getString(R.string.drops)
        }
    }

    fun fromDisplayName(context: Context, displayName: String): QuantUnit {
        return QuantUnit.entries.find {
            getDisplayName(context, it) == displayName
        } ?: throw IllegalArgumentException("No QuantUnit matches the display name: $displayName")
    }



    // Conversion rates to base units
    private val conversionRatesToBase = mapOf(
        // Mass to kilograms
        QuantUnit.milligrams to 0.000001,
        QuantUnit.grams to 0.001,
        QuantUnit.kilograms to 1.0,
        QuantUnit.tablespoons_solids_plants_powders to 0.016,
        QuantUnit.teaspoons_solids_plants_powders to 0.005,

        // Volume to liters
        QuantUnit.drops to 0.00005, // 1 drop = 0.05 mL = 0.00005 L
        QuantUnit.milliliters to 0.001,
        QuantUnit.teaspoons to 0.005,
        QuantUnit.tablespoons to 0.016,
        QuantUnit.cups to 0.25,
        QuantUnit.liters to 1.0,

        // Discrete units
        QuantUnit.whole_pieces to 1.0,
        QuantUnit.piece_about_50_grams to 0.05,
        QuantUnit.piece_about_100_grams to 0.1,
        QuantUnit.piece_about_250_grams to 0.25
    )

    // Conversion rates from base units
    private val conversionRatesFromBase = mapOf(
        // Mass from kilograms
        QuantUnit.milligrams to 1000000.0,
        QuantUnit.grams to 1000.0,
        QuantUnit.kilograms to 1.0,
        QuantUnit.tablespoons_solids_plants_powders to 62.5,
        QuantUnit.teaspoons_solids_plants_powders to 200.0,

        // Volume from liters
        QuantUnit.milliliters to 1000.0,
        QuantUnit.teaspoons to 200.0,
        QuantUnit.tablespoons to 62.5,
        QuantUnit.cups to 4.0,
        QuantUnit.liters to 1.0,
        QuantUnit.drops to 20000.0,

        // Discrete units
        QuantUnit.whole_pieces to 1.0,
        QuantUnit.piece_about_50_grams to 20.0,
        QuantUnit.piece_about_100_grams to 10.0,
        QuantUnit.piece_about_250_grams to 4.0
    )

    // Map defining the progression of units for each category
    val unitProgression = mapOf(
        // Mass units progression
        "mass" to listOf(
            QuantUnit.milligrams,
            QuantUnit.grams,
            QuantUnit.teaspoons_solids_plants_powders,
            QuantUnit.tablespoons_solids_plants_powders,
            QuantUnit.kilograms
        ),

        // Volume units progression
        "volume" to listOf(
            QuantUnit.drops,
            QuantUnit.milliliters,
            QuantUnit.teaspoons,
            QuantUnit.tablespoons,
            QuantUnit.cups,
            QuantUnit.liters
        ),

        // Discrete units progression (no hierarchy here)
        "discrete" to listOf(
            QuantUnit.piece_about_50_grams,
            QuantUnit.piece_about_100_grams,
            QuantUnit.piece_about_250_grams,
            QuantUnit.whole_pieces
        )
    )



    private fun convertFromBase(amount: Double, unit: QuantUnit): Double {
        val rate = conversionRatesFromBase[unit]
            ?: throw IllegalArgumentException("Conversion from base unit not defined for $unit")
        return amount * rate
    }

    private fun convertToBase(amount: Double, unit: QuantUnit): Double {
        val rate = conversionRatesToBase[unit]
            ?: throw IllegalArgumentException("Conversion to base unit not defined for $unit")
        return amount * rate
    }


    fun convert(amount: Double, fromUnit: QuantUnit, toUnit: QuantUnit): Double {
        // Convert from the source unit to the base unit
        val baseAmount = convertToBase(amount, fromUnit)
        // Convert from the base unit to the target unit
        return convertFromBase(baseAmount, toUnit)
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
        // If the units are the same, return the unit itself
        if (unit1 == unit2) return unit1

        // Check if both units have a defined conversion rate to the same base unit
        val baseUnit1 = conversionRatesToBase[unit1]
        val baseUnit2 = conversionRatesToBase[unit2]

        return if (baseUnit1 != null && baseUnit1 == baseUnit2) {
            // If units share the same base, return one of the units
            unit1
        } else {
            // No compatibility found
            null
        }
    }

    fun getUnitCategory(unit: QuantUnit): String? {
        return when (unit) {
            in unitProgression["mass"]!! -> "mass"
            in unitProgression["volume"]!! -> "volume"
            in unitProgression["discrete"]!! -> "discrete"
            else -> null // Unknown category
        }
    }


    fun getOptimalUnit(amount: Double, initialUnit: QuantUnit): QuantUnit {
        val category = getUnitCategory(initialUnit)
            ?: throw IllegalArgumentException("Unit $initialUnit does not belong to any defined category.")


        // Get the progression for the category
        val units = unitProgression[category] ?: return initialUnit

        var currentAmount = amount
        var currentUnit = initialUnit

        // Find the index of the initial unit in the progression list
        val startIndex = units.indexOf(initialUnit)
        if (startIndex == -1) return initialUnit // Unit not in progression list

        // Iterate through the units in ascending order
        for (i in startIndex until units.size - 1) {
            val nextUnit = units[i + 1]

            // Convert to the next unit
            val convertedAmount = convert(currentAmount, currentUnit, nextUnit)

            // Stop if the integer part disappears (i.e., becomes zero)
            if (convertedAmount.toInt() == 0) {
                return currentUnit // Return the last valid unit
            }

            // Check if the fractional part has an unacceptable pattern
            val fractionalPart = convertedAmount - convertedAmount.toInt()
            val fractionalString = fractionalPart.toString()
            if (fractionalPart > 0 && fractionalString.matches(Regex("^0\\.0+[1-9].*"))) {
                return currentUnit // Revert to the last valid unit
            }

            // Update the current unit and amount
            currentUnit = nextUnit
            currentAmount = convertedAmount
        }

        return currentUnit
    }



}