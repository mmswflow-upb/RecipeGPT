package com.example.recipegpt.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recipegpt.adapters.RecipeAdapter
import com.example.recipegpt.databinding.ActivityHomeBinding
import com.example.recipegpt.models.Recipe
import com.example.recipegpt.services.RecipeService

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var recipeAdapter: RecipeAdapter
    private var recipeService: RecipeService? = null
    private var isBound = false

    // Service connection object to manage the connection to the service
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as RecipeService.LocalBinder
            recipeService = binder.getService()
            isBound = true
            Log.d("HomeActivity", "Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            recipeService = null
            isBound = false
            Log.d("HomeActivity", "Service disconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView and Adapter
        recipeAdapter = RecipeAdapter()
        binding.recipeRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = recipeAdapter
        }

        // Bind to the RecipeService
        Intent(this, RecipeService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        // Set up search functionality with a single request
        binding.searchEditText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val query = v.text.toString().trim()
                if (query.isNotEmpty()) {
                    Log.d("Recipe Search Bar", "Query: $query")
                    searchRecipes(query)
                } else {
                    Toast.makeText(this, "Enter a search query", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unbind the service when the activity is destroyed
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    private fun searchRecipes(query: String) {
        if (isBound && recipeService != null) {
            recipeService?.searchRecipes(query) { recipes ->
                displaySearchResults(recipes)
            }
        } else {
            Log.e("HomeActivity", "Service not bound or null")
        }
    }

    private fun displaySearchResults(results: List<Recipe>) {
        if (results.isNotEmpty()) {
            recipeAdapter.submitList(results)
        } else {
            Toast.makeText(this, "No recipes found", Toast.LENGTH_SHORT).show()
        }
    }
}
