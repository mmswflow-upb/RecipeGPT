package com.example.recipegpt.fragments.shoppinglist

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.ResultReceiver
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.recipegpt.models.Ingredient
import com.example.recipegpt.models.QuantUnit
import com.example.recipegpt.models.Recipe
import com.example.recipegpt.models.UnitConverter
import com.example.recipegpt.services.DatabaseBackgroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

class ShoppingListViewModel(application: Application) : AndroidViewModel(application) {


    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    private val _shoppingList = MutableLiveData<List<Ingredient>>(emptyList())
    val shoppingList : LiveData<List<Ingredient>> get() = _shoppingList

    private val _filteredShoppingList = MutableLiveData<List<Ingredient>>(emptyList())
    val filteredShoppingList: LiveData<List<Ingredient>> get() = _filteredShoppingList

    private val _popupIngredient = MutableLiveData<Ingredient?>()
    val popupIngredient: LiveData<Ingredient?> get() = _popupIngredient

    private val _popupSelectedUnit = MutableLiveData<QuantUnit?>()
    val popupSelectedUnit: LiveData<QuantUnit?> get() = _popupSelectedUnit

    private val _query = MutableLiveData("")
    val query: LiveData<String> get() = _query


    private val databaseChangesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.recipegpt.LOCAL_DB_CHANGED") {
                // Re-fetch the shopping list whenever DB changes
                fetchListedRecipes()
                applyQueryFilter()
            }
        }
    }

    init {
        // Register the BroadcastReceiver
        val intentFilter = IntentFilter("com.example.recipegpt.LOCAL_DB_CHANGED")
        LocalBroadcastManager.getInstance(context).registerReceiver(databaseChangesReceiver, intentFilter)

        // Initial fetch when ViewModel is created
        fetchListedRecipes()
    }

    override fun onCleared() {
        super.onCleared()
        // Unregister the BroadcastReceiver to avoid leaks
        LocalBroadcastManager.getInstance(context).unregisterReceiver(databaseChangesReceiver)
    }

    private fun fetchListedRecipes() {
        CoroutineScope(Dispatchers.IO).launch {
            val listedRecipesResultReceiver = object : ResultReceiver(null) {
                override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                    val recipes = resultData?.getParcelableArrayList("recipes", Recipe::class.java) ?: emptyList()
                    val totalNecessaryIngredients = mutableMapOf<String, Ingredient>()

                    // Accumulate necessary ingredients from each listed recipe
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

                    // Now fetch the user’s saved ingredients to figure out what’s still needed
                    val savedIngredientsResultReceiver = object : ResultReceiver(null) {
                        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                            val savedIngredients =
                                resultData?.getParcelableArrayList("data", Ingredient::class.java) ?: emptyList()

                            val shoppingList = mutableListOf<Ingredient>()
                            totalNecessaryIngredients.values.forEach { necessaryIngredient ->
                                val savedIngredient = savedIngredients.find {
                                    it.item.equals(necessaryIngredient.item, ignoreCase = true)
                                }
                                if (savedIngredient != null) {
                                    val remainingAmount = maxOf(
                                        0.0,
                                        UnitConverter.convert(
                                            necessaryIngredient.amount.toDouble(),
                                            QuantUnit.valueOf(necessaryIngredient.unit),
                                            QuantUnit.valueOf(savedIngredient.unit)
                                        ) - savedIngredient.amount.toDouble()
                                    )
                                    if (abs(remainingAmount)/necessaryIngredient.amount.toDouble() > 0.01 ) { //1% max allowed difference between saved amount and required amount by recipe
                                        val oldUnit = QuantUnit.entries.first{ it.unit == savedIngredient.unit}
                                        val optimalUnit = UnitConverter.getOptimalUnit(remainingAmount, oldUnit)
                                        val convertedRemainingAmount = UnitConverter.convert(remainingAmount, oldUnit, optimalUnit)
                                        shoppingList.add(
                                            Ingredient(
                                                item = necessaryIngredient.item,
                                                amount = convertedRemainingAmount,
                                                unit = optimalUnit.unit
                                            )
                                        )
                                    }
                                } else {
                                    // If ingredient not in saved list, still needed entirely
                                    shoppingList.add(necessaryIngredient)
                                }
                            }

                            _shoppingList.postValue(shoppingList)
                            _filteredShoppingList.postValue(shoppingList)
                            applyQueryFilter() // Re-apply the query filter to the updated list
                        }
                    }
                    val savedIngredientsIntent = Intent(context, DatabaseBackgroundService::class.java).apply {
                        action = "GET_SAVED_INGREDIENTS"
                        putExtra("resultReceiver", savedIngredientsResultReceiver)
                    }
                    context.startService(savedIngredientsIntent)
                }
            }

            val fetchListedRecipesIntent = Intent(context, DatabaseBackgroundService::class.java).apply {
                action = "FETCH_LISTED_RECIPES"
                putExtra("resultReceiver", listedRecipesResultReceiver)
            }
            context.startService(fetchListedRecipesIntent)
        }
    }

    fun applyQueryFilter() {
        val filteredList  = _shoppingList.value?.filter { ingredient -> ingredient.item.contains(_query.value!!, ignoreCase = true) }
        _filteredShoppingList.postValue(filteredList ?: emptyList())
    }

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
        applyQueryFilter()
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

    fun updatePopupSelectedUnit(unit: QuantUnit) {
        _popupSelectedUnit.value = unit
    }

    fun openPopupForIngredient(ingredient: Ingredient) {
        _popupIngredient.value = ingredient
        _popupSelectedUnit.value =
            QuantUnit.entries.find { it.unit == ingredient.unit } ?: QuantUnit.whole_pieces

    }

    fun closePopup() {
        _popupIngredient.value = null
        _popupSelectedUnit.value = null
    }
}
