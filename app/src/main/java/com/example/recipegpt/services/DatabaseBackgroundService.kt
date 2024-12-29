package com.example.recipegpt.services

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.ResultReceiver
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.recipegpt.data.AppDatabase
import com.example.recipegpt.data.entities.toIngredientEntity
import com.example.recipegpt.data.entities.toIngredientModel
import com.example.recipegpt.data.entities.toRecipeEntity
import com.example.recipegpt.data.entities.toRecipeModel
import com.example.recipegpt.models.Ingredient
import com.example.recipegpt.models.QuantUnit
import com.example.recipegpt.models.Recipe
import com.example.recipegpt.models.UnitConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseBackgroundService : Service() {

    // Database instance
    private lateinit var database: AppDatabase

    // Notify listeners about database changes
    private fun notifyDatabaseChanged() {
        val intent = Intent("com.example.recipegpt.LOCAL_DB_CHANGED")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        Log.d("DBBackgroundService-notifyDatabaseChanged", "Notifying other viewmodels of the changes in the db")
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(applicationContext)
        Log.d("DBBackgroundService-onCreate", "database initialized")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val resultReceiver = intent?.getParcelableExtra("resultReceiver", ResultReceiver::class.java)

        when (action) {
            "GET_SAVED_RECIPES" -> getSavedRecipes(resultReceiver)
            "GET_RECIPE_BY_NAME" -> {
                val name = intent.getStringExtra("name")

                if (name != null) getSavedRecipeByName(name, resultReceiver)
            }
            "SAVE_OR_REPLACE_RECIPE" -> {
                val recipe = intent.getParcelableExtra("recipe", Recipe::class.java)
                Log.d("DBService-onStart", "Save or replacing: ${recipe?.title} with listing : ${recipe?.listed}")
                if (recipe != null) saveOrReplaceRecipe(recipe, resultReceiver)
            }
            "DELETE_RECIPE_BY_NAME" -> {
                val name = intent.getStringExtra("name")
                if (name != null) deleteRecipeByName(name, resultReceiver)
            }
            "GET_SAVED_INGREDIENTS" -> getSavedIngredients(resultReceiver)
            "GET_INGREDIENT_BY_NAME_AND_UNIT" -> {
                val name = intent.getStringExtra("name")
                val unit = intent.getStringExtra("unit")

                if (name != null && unit != null) getSavedIngredientByNameAndUnit(name, unit, resultReceiver)
            }
            "SAVE_OR_UPDATE_INGREDIENT" -> {
                val ingredient = intent.getParcelableExtra("ingredient", Ingredient::class.java)
                if (ingredient != null) saveOrUpdateIngredient(ingredient, resultReceiver)
            }
            "DELETE_INGREDIENT_BY_NAME_AND_UNIT" -> {
                val name = intent.getStringExtra("name")
                val unit = intent.getStringExtra("unit")
                if (name != null && unit != null) deleteIngredientByNameAndUnit(name, unit,  resultReceiver)
            }
            "CAN_COOK_RECIPE" -> {
                val recipe = intent.getParcelableExtra("recipe", Recipe::class.java)
                if (recipe != null) canCookRecipe(recipe, resultReceiver)
            }
            "COOK_RECIPE" -> {
                val recipe = intent.getParcelableExtra("recipe", Recipe::class.java)
                if (recipe != null) cookRecipe(recipe, resultReceiver)
            }
            "FETCH_LISTED_RECIPES" -> {
                 fetchListedRecipes(resultReceiver)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun getSavedRecipes(resultReceiver: ResultReceiver?) {
        CoroutineScope(Dispatchers.IO).launch {
            val recipes = database.recipeDao().getAllRecipesSync()

            val recipeModels = recipes.map { it.toRecipeModel() }
            val bundle = Bundle().apply {
                putParcelableArrayList("data", ArrayList(recipeModels))
            }

            resultReceiver?.send(0, bundle)
        }
    }

    private fun getSavedRecipeByName(name: String, resultReceiver: ResultReceiver?) {
        CoroutineScope(Dispatchers.IO).launch {
            val recipeEntity = database.recipeDao().getRecipeByNameSync(name)
            val recipe = recipeEntity?.toRecipeModel()
            val bundle = Bundle().apply {
                putParcelable("data", recipe)
            }
            resultReceiver?.send(0, bundle)

        }
    }

    private fun saveOrReplaceRecipe(recipe: Recipe, resultReceiver: ResultReceiver?) {
        CoroutineScope(Dispatchers.IO).launch {
            val recipeEntity = recipe.toRecipeEntity()
            database.recipeDao().insertRecipe(recipeEntity)
            resultReceiver?.send(0, null)
            notifyDatabaseChanged()
        }
    }

    private fun deleteRecipeByName(name: String, resultReceiver: ResultReceiver?) {
        CoroutineScope(Dispatchers.IO).launch {
            database.recipeDao().deleteRecipeByName(name)
            resultReceiver?.send(0, null)
        }
        notifyDatabaseChanged()

    }

    private fun getSavedIngredients(resultReceiver: ResultReceiver?) {
        CoroutineScope(Dispatchers.IO).launch {
            val ingredients = database.ingredientDao().getAllIngredientsSync()
            val ingredientModels = ingredients.map { it.toIngredientModel() }
            val bundle = Bundle().apply {
                putParcelableArrayList("data", ArrayList(ingredientModels))
            }
            resultReceiver?.send(0, bundle)
        }
    }

    private fun fetchListedRecipes(resultReceiver: ResultReceiver?) {
        CoroutineScope(Dispatchers.IO).launch {
            val recipes = database.recipeDao().getListedRecipes()
            val recipeList = recipes.map { it.toRecipeModel() } // Convert to Recipe model
            val bundle = Bundle().apply {
                putParcelableArrayList("recipes", ArrayList(recipeList))
            }
            resultReceiver?.send(0, bundle)
        }
    }


    private fun canCookRecipe(recipe: Recipe, resultReceiver: ResultReceiver?) {
        CoroutineScope(Dispatchers.IO).launch {
            val savedIngredients = database.ingredientDao().getAllIngredientsSync()

            val insufficientIngredients = recipe.ingredients.filter { required ->
                val saved = savedIngredients.find {
                    it.item.equals(required.item, ignoreCase = true)
                }
                if (saved != null) {
                    try {
                        val requiredAmountInSavedUnit = UnitConverter.convert(
                            required.amount.toDouble(),
                            QuantUnit.valueOf(required.unit),
                            QuantUnit.valueOf(saved.unit)
                        )
                        requiredAmountInSavedUnit /saved.amount < 0.99 //1% allowed difference
                    } catch (e: IllegalArgumentException) {
                        true // Treat as insufficient if conversion fails
                    }
                } else {
                    true // Ingredient not found
                }
            }

            val canCook = insufficientIngredients.isEmpty()
            val bundle = Bundle().apply {
                putBoolean("canCook", canCook)
                if (!canCook) {
                    putParcelableArrayList("missing_ingredients", ArrayList(insufficientIngredients))
                }
            }
            resultReceiver?.send(0, bundle)
        }
    }

    private fun cookRecipe(recipe: Recipe, resultReceiver: ResultReceiver?) {
        CoroutineScope(Dispatchers.IO).launch {
            val savedIngredients = database.ingredientDao().getAllIngredientsSync()

            recipe.ingredients.forEach { required ->
                val saved = savedIngredients.find {
                    it.item.equals(required.item, ignoreCase = true)
                }
                if (saved != null) {
                    try {
                        val requiredAmountInSavedUnit = UnitConverter.convert(
                            required.amount.toDouble(),
                            QuantUnit.valueOf(required.unit),
                            QuantUnit.valueOf(saved.unit)
                        )

                        val updatedAmount = saved.amount - requiredAmountInSavedUnit
                        if (updatedAmount > 0) {
                            val optimalUnit = UnitConverter.getOptimalUnit(updatedAmount, QuantUnit.valueOf(saved.unit))
                            val normalizedAmount = UnitConverter.convert(updatedAmount, QuantUnit.valueOf(saved.unit), optimalUnit)

                            val updatedIngredient = saved.copy(
                                amount = normalizedAmount,
                                unit = optimalUnit.name
                            )
                            database.ingredientDao().updateIngredient(updatedIngredient)
                        } else {
                            database.ingredientDao().deleteIngredientByNameAndUnit(saved.item, saved.unit)
                        }
                    } catch (e: IllegalArgumentException) {
                        Log.e("cookRecipe", "Unit conversion failed for ${required.item}")
                    }
                }
            }

            resultReceiver?.send(0, Bundle().apply {
                putBoolean("success", true)
            })
            notifyDatabaseChanged()
        }
    }


    private fun getSavedIngredientByNameAndUnit(name: String, unit: String, resultReceiver: ResultReceiver?) {
        CoroutineScope(Dispatchers.IO).launch {
            val ingredientEntity = database.ingredientDao().getIngredientByNameAndUnitSync(name, unit)
            val ingredient = ingredientEntity?.toIngredientModel()
            val bundle = Bundle().apply {
                putParcelable("data", ingredient)
            }
            resultReceiver?.send(0, bundle)
        }
    }


    private fun saveOrUpdateIngredient(ingredient: Ingredient, resultReceiver: ResultReceiver?) {
        CoroutineScope(Dispatchers.IO).launch {
            val ingredientEntity = ingredient.toIngredientEntity()
            val existingIngredient = database.ingredientDao().getIngredientByNameAndUnitSync(ingredientEntity.item, ingredientEntity.unit)

            if (existingIngredient != null) {
                // Use UnitConverter to add the amounts
                val (updatedAmount, updatedUnit) = UnitConverter.add(
                    existingIngredient.amount,
                    QuantUnit.valueOf(existingIngredient.unit),
                    ingredientEntity.amount,
                    QuantUnit.valueOf(ingredientEntity.unit)
                )

                val updatedIngredient = existingIngredient.copy(
                    amount = updatedAmount,
                    unit = updatedUnit.name
                )
                database.ingredientDao().updateIngredient(updatedIngredient)
            } else {
                // Normalize the ingredient before inserting
                val optimalUnit = UnitConverter.getOptimalUnit(
                    ingredientEntity.amount,
                    QuantUnit.valueOf(ingredientEntity.unit)
                )
                val normalizedAmount = UnitConverter.convert(
                    ingredientEntity.amount,
                    QuantUnit.valueOf(ingredientEntity.unit),
                    optimalUnit
                )

                val normalizedIngredient = ingredientEntity.copy(
                    amount = normalizedAmount,
                    unit = optimalUnit.name
                )
                database.ingredientDao().insertIngredient(normalizedIngredient)
            }

            resultReceiver?.send(0, null)
            notifyDatabaseChanged()
        }
    }

    private fun deleteIngredientByNameAndUnit(name: String, unit: String, resultReceiver: ResultReceiver?) {
        CoroutineScope(Dispatchers.IO).launch {
            database.ingredientDao().deleteIngredientByNameAndUnit(name, unit)
            resultReceiver?.send(0, null)
            notifyDatabaseChanged()
        }
    }

}
