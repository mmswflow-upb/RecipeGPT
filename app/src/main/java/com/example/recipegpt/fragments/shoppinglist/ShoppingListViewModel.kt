package com.example.recipegpt.fragments.shoppinglist

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.recipegpt.models.Ingredient
import com.example.recipegpt.models.QuantUnit
import com.example.recipegpt.models.Recipe
import com.example.recipegpt.models.UnitConverter
import com.example.recipegpt.services.DatabaseBackgroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShoppingListViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    private val _shoppingList = MutableLiveData<List<Ingredient>>()
    val shoppingList: LiveData<List<Ingredient>> get() = _shoppingList

    private val _popupIngredient = MutableLiveData<Ingredient?>()
    val popupIngredient: LiveData<Ingredient?> get() = _popupIngredient

    fun fetchShoppingList() {
        CoroutineScope(Dispatchers.IO).launch {
            // Step 1: Fetch saved recipes from the database
            val recipeResultReceiver = object : ResultReceiver(null) {
                override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                    val recipes = resultData?.getParcelableArrayList("data", Recipe::class.java) ?: emptyList()

                    // Step 2: Extract and aggregate ingredients from all recipes
                    val totalNecessaryIngredients = mutableMapOf<String, Ingredient>()
                    recipes.forEach { recipe ->
                        recipe.ingredients.forEach { ingredient ->
                            val existing = totalNecessaryIngredients[ingredient.item]
                            if (existing != null) {
                                val (totalAmount, normalizedUnit) = UnitConverter.add(
                                    existing.amount.toDouble(),
                                    QuantUnit.valueOf(existing.unit),
                                    ingredient.amount.toDouble(),
                                    QuantUnit.valueOf(ingredient.unit)
                                )
                                totalNecessaryIngredients[ingredient.item] = Ingredient(
                                    item = ingredient.item,
                                    amount = totalAmount,
                                    unit = normalizedUnit.unit
                                )
                            } else {
                                totalNecessaryIngredients[ingredient.item] = ingredient
                            }
                        }
                    }

                    // Step 3: Fetch saved ingredients from the database
                    val savedIngredientsResultReceiver = object : ResultReceiver(null) {
                        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                            val savedIngredients =
                                resultData?.getParcelableArrayList("data", Ingredient::class.java) ?: emptyList()

                            // Step 4: Subtract saved amounts from total necessary
                            val shoppingList = mutableListOf<Ingredient>()
                            totalNecessaryIngredients.values.forEach { necessaryIngredient ->
                                val savedIngredient = savedIngredients.find { it.item.equals(necessaryIngredient.item, ignoreCase = true) }

                                if (savedIngredient != null) {
                                    val remainingAmount = maxOf(
                                        0.0,
                                        UnitConverter.convert(
                                            necessaryIngredient.amount.toDouble(),
                                            QuantUnit.valueOf(necessaryIngredient.unit),
                                            QuantUnit.valueOf(savedIngredient.unit)
                                        ) - savedIngredient.amount.toDouble()
                                    )

                                    if (remainingAmount > 0) {
                                        shoppingList.add(
                                            Ingredient(
                                                item = necessaryIngredient.item,
                                                amount = remainingAmount,
                                                unit = savedIngredient.unit
                                            )
                                        )
                                    }
                                } else {
                                    shoppingList.add(necessaryIngredient) // Fully needed if not saved
                                }
                            }

                            // Step 5: Update LiveData with the final shopping list
                            _shoppingList.postValue(shoppingList)
                            _popupIngredient.postValue(null) // Close the popup when list updates
                        }
                    }

                    // Fetch saved ingredients
                    val savedIngredientsIntent = Intent(context, DatabaseBackgroundService::class.java).apply {
                        action = "GET_SAVED_INGREDIENTS"
                        putExtra("resultReceiver", savedIngredientsResultReceiver)
                    }
                    context.startService(savedIngredientsIntent)
                }
            }

            // Fetch saved recipes
            val recipesIntent = Intent(context, DatabaseBackgroundService::class.java).apply {
                action = "GET_SAVED_RECIPES"
                putExtra("resultReceiver", recipeResultReceiver)
            }
            context.startService(recipesIntent)
        }
    }


    fun addToDatabase(ingredient: Ingredient) {
        CoroutineScope(Dispatchers.IO).launch {
            val intent = Intent(context, DatabaseBackgroundService::class.java).apply {
                action = "SAVE_OR_UPDATE_INGREDIENT"
                putExtra("ingredient", ingredient)
            }
            context.startService(intent)
        }
    }

    fun openPopupForIngredient(ingredient: Ingredient) {
        _popupIngredient.postValue(ingredient)
    }

    fun closePopup() {
        _popupIngredient.postValue(null)
    }
}
