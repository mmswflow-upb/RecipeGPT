package com.example.recipegpt.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.recipegpt.R
import com.example.recipegpt.databinding.ShoppingListItemIngredientBinding
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
        val binding = ShoppingListItemIngredientBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return IngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(ingredients[position])
    }

    override fun getItemCount(): Int = ingredients.size

    inner class IngredientViewHolder(
        private val binding: ShoppingListItemIngredientBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ingredient: Ingredient) {
            binding.ingredientName.text = ingredient.item

            // Handle unit display
            val unitDisplay = UnitConverter.getDisplayName(
                context,
                QuantUnit.entries.first { entry -> entry.unit == ingredient.unit }
            )

            binding.ingredientAmount.text = context.getString(
                R.string.ingredient_amount_placeholder,
                ingredient.amount,
                unitDisplay
            )

            // Handle cart button click
            binding.cartButton.setOnClickListener {
                onCartButtonClicked(ingredient)
            }
        }
    }
}