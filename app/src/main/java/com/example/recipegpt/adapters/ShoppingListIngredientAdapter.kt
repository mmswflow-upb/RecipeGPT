package com.example.recipegpt.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.recipegpt.R
import com.example.recipegpt.models.Ingredient
import com.example.recipegpt.models.QuantUnit

class ShoppingListIngredientAdapter(
    private val context: Context,
    private val onCartButtonClicked: (Ingredient) -> Unit
) : RecyclerView.Adapter<ShoppingListIngredientAdapter.IngredientViewHolder>() {

    private val ingredients = mutableListOf<Ingredient>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newIngredients: List<Ingredient>) {
        ingredients.clear()
        ingredients.addAll(newIngredients)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.shopping_list_item_ingredient, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(ingredients[position])
    }

    override fun getItemCount(): Int = ingredients.size

    inner class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ingredientName: TextView = itemView.findViewById(R.id.ingredientName)
        private val ingredientAmount: TextView = itemView.findViewById(R.id.ingredientAmount)
        private val cartButton: ImageButton = itemView.findViewById(R.id.cartButton)

        fun bind(ingredient: Ingredient) {
            ingredientName.text = ingredient.item

            // Handle unit display
            val unitDisplay = convertUnitDisplay(ingredient.unit)

            ingredientAmount.text = context.getString(
                R.string.ingredient_amount_placeholder,
                ingredient.amount,
                unitDisplay
            )

            // Handle cart button click
            cartButton.setOnClickListener {
                onCartButtonClicked(ingredient)
            }
        }
    }

    // Converts unit to display-friendly format
    private fun convertUnitDisplay(unit: String): String {
        return when (unit) {
            QuantUnit.tablespoons_solids_plants_powders.unit -> context.getString(R.string.tablespoons)
            QuantUnit.teaspoons_solids_plants_powders.unit -> context.getString(R.string.teaspoons)
            QuantUnit.whole_pieces.unit -> context.getString(R.string.whole_pieces)
            QuantUnit.piece_about_50_grams.unit -> context.getString(R.string.piece_about_50_grams)
            QuantUnit.piece_about_100_grams.unit -> context.getString(R.string.piece_about_100_grams)
            QuantUnit.piece_about_250_grams.unit -> context.getString(R.string.piece_about_250_grams)
            else -> unit
        }
    }
}

