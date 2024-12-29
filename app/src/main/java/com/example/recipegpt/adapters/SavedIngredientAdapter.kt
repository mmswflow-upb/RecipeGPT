package com.example.recipegpt.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.recipegpt.R
import com.example.recipegpt.databinding.SavedListItemIngredientBinding
import com.example.recipegpt.models.Ingredient
import com.example.recipegpt.models.QuantUnit
import com.example.recipegpt.models.UnitConverter



class SavedIngredientAdapter(
    private val context: Context,
    private val onEditButtonClicked: (Ingredient) -> Unit,
    private val onDeleteButtonClicked : (Ingredient) -> Unit
) : RecyclerView.Adapter<SavedIngredientAdapter.IngredientViewHolder>() {

    private val ingredients = mutableListOf<Ingredient>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newIngredients: List<Ingredient>) {
        ingredients.clear()
        ingredients.addAll(newIngredients)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val binding = SavedListItemIngredientBinding.inflate(
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
        private val binding: SavedListItemIngredientBinding
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

            // Handle edit button click
            binding.editButton.setOnClickListener {
                onEditButtonClicked(ingredient)
            }

            binding.deleteButton.setOnClickListener{
                onDeleteButtonClicked(ingredient)
            }
        }
    }
}