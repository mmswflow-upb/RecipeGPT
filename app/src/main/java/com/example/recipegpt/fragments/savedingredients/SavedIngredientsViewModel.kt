package com.example.recipegpt.fragments.savedingredients

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.recipegpt.models.Ingredient
import com.example.recipegpt.models.QuantUnit
import com.example.recipegpt.services.DatabaseBackgroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SavedIngredientsViewModel(application: Application) : AndroidViewModel(application) {
    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    private val _ingredientsList = MutableLiveData<List<Ingredient>>(emptyList())
    val ingredientsList : LiveData<List<Ingredient>> get() = _ingredientsList

    private val _filteredIngredientsList = MutableLiveData<List<Ingredient>>(emptyList())
    val filteredIngredientsList: LiveData<List<Ingredient>> get() = _filteredIngredientsList

    private val _popupIngredient = MutableLiveData<Ingredient?>()
    val popupIngredient: LiveData<Ingredient?> get() = _popupIngredient

    private val _popupSelectedUnit = MutableLiveData<QuantUnit?>()
    val popupSelectedUnit: LiveData<QuantUnit?> get() = _popupSelectedUnit

    private val _query = MutableLiveData("")
    val query: LiveData<String> get() = _query

    /**
     * BroadcastReceiver to listen for local database changes.
     * Whenever we receive the "com.example.recipegpt.LOCAL_DB_CHANGED" action,
     * we refetch the saved ingredients list
     */
    private val databaseChangesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.recipegpt.LOCAL_DB_CHANGED") {
                // Re-fetch the shopping list whenever DB changes
                Log.d("ShoppingListViewModel", "Local DB changed broadcast received.")
                fetchSavedIngredients()
                applyQueryFilter()
            }
        }
    }

    init {
        // Register the BroadcastReceiver
        val intentFilter = IntentFilter("com.example.recipegpt.LOCAL_DB_CHANGED")
        LocalBroadcastManager.getInstance(context).registerReceiver(databaseChangesReceiver, intentFilter)

        // Initial fetch when ViewModel is created
        fetchSavedIngredients()
    }

    override fun onCleared() {
        super.onCleared()
        // Unregister the BroadcastReceiver to avoid leaks
        LocalBroadcastManager.getInstance(context).unregisterReceiver(databaseChangesReceiver)
    }


    fun applyQueryFilter() {
        Log.d("applyQueryFilter", "Filtering listed ingredients")
        val filteredList  = _ingredientsList.value?.filter { ingredient -> ingredient.item.contains(_query.value!!, ignoreCase = true) }
        _filteredIngredientsList.postValue(filteredList ?: emptyList())
    }

    fun updateQuery(newQuery: String) {
        Log.d("updateQuery", "Updating filter query")
        _query.value = newQuery
        applyQueryFilter()
    }


    fun fetchSavedIngredients(){
        CoroutineScope(Dispatchers.IO).launch {
            val savedIngredientsResultReceiver : ResultReceiver = object: ResultReceiver(null){
                override fun onReceiveResult(resultCode: Int, resultData: Bundle?){

                    val ingredients = resultData?.getParcelableArrayList("data", Ingredient::class.java) ?: emptyList()
                    _ingredientsList.postValue(ingredients)
                    _filteredIngredientsList.postValue(ingredients)
                    applyQueryFilter()
                }

            }

            val savedIngredientsIntent = Intent(context, DatabaseBackgroundService::class.java).apply {
                action = "GET_SAVED_INGREDIENTS"
                putExtra("resultReceiver", savedIngredientsResultReceiver)
            }
            context.startService(savedIngredientsIntent)
        }

    }

    fun editIngredientInDatabase(ingredient: Ingredient) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("addToDatabase", "Adding or updating ingredient in DB: ${ingredient.item}")
            val intent = Intent(context, DatabaseBackgroundService::class.java).apply {
                action = "SAVE_OR_UPDATE_INGREDIENT"
                putExtra("ingredient", ingredient)
            }
            context.startService(intent)
        }
    }

    fun deleteIngredient(ingredient: Ingredient){

        CoroutineScope(Dispatchers.IO).launch {
            Log.d("deleting ingredient", "The deleted ingredient: ${ingredient.item}")
            val intent = Intent(context, DatabaseBackgroundService::class.java).apply{
                action = "DELETE_INGREDIENT_BY_NAME_AND_UNIT"
                putExtra("name", ingredient.item)
                putExtra("unit", ingredient.unit)
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