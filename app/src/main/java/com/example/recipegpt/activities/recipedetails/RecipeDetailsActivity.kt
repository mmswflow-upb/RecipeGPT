package com.example.recipegpt.activities.recipedetails

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.recipegpt.R
import com.example.recipegpt.databinding.ActivityRecipeDetailsBinding
import com.example.recipegpt.models.Recipe

class RecipeDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailsBinding
    private val viewModel: RecipeDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the recipe passed via intent
        val recipe = intent.getParcelableExtra("recipe", Recipe::class.java)
        if (recipe != null) {
            viewModel.setRecipe(recipe)
        }

        // Observe the recipe LiveData and update the UI
        viewModel.recipe.observe(this) { currentRecipe ->
            if (currentRecipe != null) {
                displayRecipeDetails(currentRecipe)
            }
        }

        // Handle button clicks
        binding.saveButton.setOnClickListener {
            // Save functionality (to be implemented)
        }

        binding.cookButton.setOnClickListener {
            // Cook functionality (to be implemented)
        }

        //Share button
        binding.shareButton.setOnClickListener {
            shareRecipeDetails()
        }
    }

    private fun displayRecipeDetails(recipe: Recipe) {
        // Populate static fields
        binding.recipeTitle.text = recipe.title
        binding.recipeCookingTime.text = getString(R.string.cooking_time_placeholder, recipe.estimatedCookingTime)
        binding.recipeServings.text = getString(R.string.servings_placeholder, recipe.servings)

        // Populate ingredients
        binding.ingredientsList.removeAllViews()
        recipe.ingredients.forEach { ingredient ->
            val ingredientView = layoutInflater.inflate(R.layout.item_ingredient, binding.ingredientsList, false)
            val ingredientBinding = com.example.recipegpt.databinding.ItemIngredientBinding.bind(ingredientView)
            ingredientBinding.ingredientName.text = ingredient.item
            ingredientBinding.ingredientAmount.text = getString(
                R.string.ingredient_amount_placeholder,
                ingredient.amount,
                ingredient.unit
            )
            binding.ingredientsList.addView(ingredientView)
        }

        // Populate instructions

        binding.instructionsList.removeAllViews()
        recipe.instructions.forEachIndexed { index, instruction ->
            val instructionView = layoutInflater.inflate(R.layout.item_instruction, binding.instructionsList, false)
            val instructionBinding = com.example.recipegpt.databinding.ItemInstructionBinding.bind(instructionView)
            instructionBinding.instructionStep.text =
                getString(R.string.instruction_step_placeholder, index + 1, instruction)
            binding.instructionsList.addView(instructionView)
        }



    }

    private fun shareRecipeDetails() {

        val recipe = viewModel.recipe.value!!

        val recipeDetails = buildRecipeDetailsText(recipe)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Check out this recipe: ${recipe.title}")
            putExtra(Intent.EXTRA_TEXT, recipeDetails)
        }

        startActivity(Intent.createChooser(shareIntent, "Share Recipe"))
    }

    private fun buildRecipeDetailsText(recipe: Recipe): String {
        val ingredients = recipe.ingredients.joinToString("\n") {
            "- ${it.amount} ${it.unit} of ${it.item}"
        }

        val instructions = recipe.instructions.joinToString("\n") { step ->
            "Step ${recipe.instructions.indexOf(step) + 1}: $step"
        }

        return """
            Recipe: ${recipe.title}
            Cooking Time: ${recipe.estimatedCookingTime} minutes
            Servings: ${recipe.servings}

            Ingredients:
            $ingredients

            Instructions:
            $instructions
        """.trimIndent()
    }
}
