package com.example.recipegpt.activities.recipedetails

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.recipegpt.R
import com.example.recipegpt.databinding.ActivityRecipeDetailsBinding
import com.example.recipegpt.models.QuantUnit
import com.example.recipegpt.models.Recipe
import com.example.recipegpt.models.UnitConverter

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
                updateListIngredientsButton(currentRecipe.listed)
            }
        }

        // Observe the saved status to update the save button
        // Observe the saved status to update the save button and toggle `listIngredients`
        viewModel.isRecipeSaved.observe(this) { isSaved ->
            updateSaveButton(isSaved)
            toggleButtonsVisibility(isSaved)
        }

        //Update the colors of the ingredient amounts in case there's a change in the saved ingredients DB
        viewModel.ingredientAvailability.observe(this) {
                updateIngredientsList()

        }

        viewModel.canBeCooked.observe(this) { canBeCooked ->
            Log.d("RecipeDetailsActivity", "Can be cooked value changed: $canBeCooked")
            updateCookButton(canBeCooked)
        }

        // Save button handler
        binding.saveButton.setOnClickListener {

            viewModel.toggleRecipeSavedStatus()
        }



        binding.listIngredients.setOnClickListener {
            viewModel.toggleRecipeListedStatus()
            updateIngredientsList()
        }



        binding.cookButton.setOnClickListener {
            viewModel.cookRecipe { success ->
                val message = if (success) {
                    getString(R.string.recipe_cooked_successfully)
                } else {
                    getString(R.string.not_enough_ingredients)
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }



        binding.shareButton.setOnClickListener {
            shareRecipeDetails()
        }
    }
    private fun getThemePrimaryColor(): Int {
        val typedValue = TypedValue()
        val theme = theme // Fetch the current theme
        val isResolved = theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)

        return if (isResolved) {
            typedValue.data // Return the color value if resolved
        } else {
            resources.getColor(android.R.color.black, null) // Fallback to black if not resolved
        }
    }

    private fun updateIngredientsList(){
        Log.d("updateIngredientsList", "Updating list of ingredients")
        // Remove old views when updating the list of ingredients

        val availability = viewModel.ingredientAvailability.value!!
        val recipe = viewModel.recipe.value

        binding.ingredientsList.removeAllViews()
        recipe?.ingredients?.forEach { ingredient ->
            val ingredientView = layoutInflater.inflate(R.layout.recipe_details_item_ingredient, binding.ingredientsList, false)
            val ingredientBinding = com.example.recipegpt.databinding.RecipeDetailsItemIngredientBinding.bind(ingredientView)

            // Convert the unit display
            val displayUnit = UnitConverter.getDisplayName(applicationContext, QuantUnit.entries.first { entry -> entry.unit == ingredient.unit  })



            ingredientBinding.ingredientName.text = ingredient.item
            ingredientBinding.ingredientAmount.text = getString(
                R.string.ingredient_amount_placeholder,
                ingredient.amount,
                displayUnit
            )
            val isAvailable = availability[ingredient.item] ?: false
            ingredientBinding.ingredientAmount.setTextColor(
                if (isAvailable)
                {
                    resources.getColor(R.color.greenPrimary, null)
                }
                else{
                    resources.getColor(R.color.redDark, null)
                }

            )
            binding.ingredientsList.addView(ingredientView)
        }
    }

    private fun updateCookButton(canBeCooked: Boolean) {
        val primaryColor = getThemePrimaryColor()
        val disabledColor = resources.getColor(R.color.grayDark, null)

        val color = if (canBeCooked) primaryColor else disabledColor

        binding.cookButton.backgroundTintList = ColorStateList.valueOf(color)
        binding.cookButton.imageTintList = ColorStateList.valueOf(color)
        binding.cookButton.isEnabled = canBeCooked
    }


    private fun updateListIngredientsButton(isListed: Boolean) {

        val color = if (isListed) resources.getColor(R.color.greenPrimary, null)
        else resources.getColor(R.color.redLight, null)
        binding.listIngredients.imageTintList = ColorStateList.valueOf(
            color)

        binding.listIngredients.backgroundTintList = ColorStateList.valueOf(
            color)
    }

    private fun displayRecipeDetails(recipe: Recipe) {
        // Populate static fields
        binding.recipeTitle.text = recipe.title
        binding.recipeCookingTime.text = getString(R.string.cooking_time_placeholder, recipe.estimatedCookingTime)
        binding.recipeServings.text = getString(R.string.servings_placeholder, recipe.servings)

        // Populate ingredients
        binding.ingredientsList.removeAllViews()
        recipe.ingredients.forEach { ingredient ->
            val ingredientView = layoutInflater.inflate(R.layout.recipe_details_item_ingredient, binding.ingredientsList, false)
            val ingredientBinding = com.example.recipegpt.databinding.RecipeDetailsItemIngredientBinding.bind(ingredientView)

            // Convert the unit display
            val displayUnit = UnitConverter.getDisplayName(applicationContext, QuantUnit.entries.first { entry -> entry.unit == ingredient.unit  })


            ingredientBinding.ingredientName.text = ingredient.item
            ingredientBinding.ingredientAmount.text = getString(
                R.string.ingredient_amount_placeholder,
                ingredient.amount,
                displayUnit
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


    private fun toggleButtonsVisibility(isSaved: Boolean) {
        if(isSaved){
            binding.cookButton.visibility = View.VISIBLE
            binding.listIngredients.visibility = View.VISIBLE

        }else{
            binding.cookButton.visibility = View.GONE
            binding.listIngredients.visibility = View.GONE
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
            val displayUnit = UnitConverter.getDisplayName(applicationContext, QuantUnit.entries.first { entry -> entry.unit == it.unit  })
            "- ${it.amount} $displayUnit of ${it.item}"
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
        Log.d("RecipeDetails-updateSaveButton", "Is recipe saved: $isSaved")
        if (isSaved) {
            binding.saveButton.setImageResource(R.drawable.ic_recipe_saved)


        } else {

            binding.saveButton.setImageResource(R.drawable.ic_recipe_not_saved)

        }
    }
}
