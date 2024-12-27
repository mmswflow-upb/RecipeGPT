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
import com.example.recipegpt.models.UnitConverter

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
            val unitDisplay = UnitConverter.getDisplayName(context, QuantUnit.entries.first { entry -> entry.unit == ingredient.unit  })

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



}

