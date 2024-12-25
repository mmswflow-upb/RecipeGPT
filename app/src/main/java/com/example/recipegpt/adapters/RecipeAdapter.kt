package com.example.recipegpt.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.recipegpt.R
import com.example.recipegpt.activities.recipedetails.RecipeDetailsActivity
import com.example.recipegpt.databinding.ItemRecipeBinding
import com.example.recipegpt.models.Recipe

class RecipeAdapter(private val context: Context) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    private val recipes = mutableListOf<Recipe>()

    fun submitList(newRecipes: List<Recipe>) {
        recipes.clear()
        recipes.addAll(newRecipes)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size

    inner class RecipeViewHolder(private val binding: ItemRecipeBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            // Set title and description
            binding.recipeTitle.text = recipe.title
            binding.recipeDescription.text = context.getString(
                R.string.recipe_description_placeholder,
                recipe.servings,
                recipe.estimatedCookingTime
            )

            // Set click listener for the Details button
            binding.detailsButton.setOnClickListener {
                val intent = Intent(context, RecipeDetailsActivity::class.java).apply {
                    putExtra("recipe", recipe) // Pass the recipe object
                }
                context.startActivity(intent)
            }
        }
    }
}
