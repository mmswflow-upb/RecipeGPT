package com.example.recipegpt.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recipegpt.adapters.RecipeAdapter
import com.example.recipegpt.databinding.ActivityHomeBinding
import com.example.recipegpt.services.RecipeService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recipeService: RecipeService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up View Binding
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // Initialize RecyclerView and Adapter
        recipeAdapter = RecipeAdapter()
        binding.recipeRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = recipeAdapter
        }

        // Fetch recipes when the activity starts

        binding.searchEditText.setOnEditorActionListener { v, _, _ ->
            val query = v.text.toString()
            if (query.isNotEmpty()) {

                startRecipeService(query) // Start the service with the query
            } else {
                Toast.makeText(this, "Enter a search query", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    /**
     * Starts the RecipeService.
     */
    private fun startRecipeService(query: String) {
        val intent = Intent(this, RecipeService::class.java).apply {
            putExtra("query", query) // Pass the query as an extra
        }
        startService(intent) // Start the service
    }



    private fun searchRecipes(query: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val recipes = recipeService.searchRecipes(query)
                if (recipes.isNotEmpty()) {
                    recipeAdapter.submitList(recipes)
                } else {
                    Toast.makeText(this@HomeActivity, "No results found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@HomeActivity, "Error searching recipes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
