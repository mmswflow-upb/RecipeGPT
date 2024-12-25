package com.example.recipegpt.activities.recipedetails

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
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

        // Observe the saved status to update the save button
        viewModel.isRecipeSaved.observe(this) { isSaved ->
            updateSaveButton(isSaved)
        }

        viewModel.ingredientAvailability.observe(this) { availability ->
            //Remove old views when updating the list of ingredients (checking if there's enough of each ingredient or not)
            binding.ingredientsList.removeAllViews()
            recipe?.ingredients?.forEach { ingredient ->
                val ingredientView = layoutInflater.inflate(R.layout.item_ingredient, binding.ingredientsList, false)
                val ingredientBinding = com.example.recipegpt.databinding.ItemIngredientBinding.bind(ingredientView)
                ingredientBinding.ingredientName.text = ingredient.item
                ingredientBinding.ingredientAmount.text = getString(
                    R.string.ingredient_amount_placeholder,
                    ingredient.amount,
                    ingredient.unit
                )
                val isAvailable = availability[ingredient.item] ?: false
                ingredientBinding.ingredientAmount.setTextColor(if (isAvailable) resources.getColor(R.color.greenPrimary, null) else resources.getColor(R.color.redDark, null))
                binding.ingredientsList.addView(ingredientView)
            }
        }


        // Save button handler
        binding.saveButton.setOnClickListener {

            viewModel.toggleRecipeSavedStatus()
        }

        binding.cookButton.setOnClickListener {
            // Cook functionality (to be implemented)
        }

        binding.shareButton.setOnClickListener {
            shareRecipeDetails()
        }
    }

    //Populate the lists with ingredients and steps
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
        val recipe = viewModel.recipe.value ?: return
        val recipeDetails = buildRecipeDetailsText(recipe)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Check out this recipe: ${recipe.title}")
            putExtra(Intent.EXTRA_TEXT, recipeDetails)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Recipe"))
    }

    //Building the text containing recipe details to share it through other apps
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

    private fun updateSaveButton(isSaved: Boolean) {
        Log.d("RecipeDetailsViewModel-updateSaveButton", "Is recipe saved: $isSaved")
        if (isSaved) {
            binding.saveButton.text = getString(R.string.unsave_button_text)
            binding.saveButton.setTextColor(resources.getColor(R.color.redLight, null))
            binding.saveButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.redLight, null))


        } else {
            binding.saveButton.text = getString(R.string.save_button_text)
            binding.saveButton.setTextColor(resources.getColor(R.color.greenPrimary, null))
            binding.saveButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.greenPrimary, null))


        }
    }
}
