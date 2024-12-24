package com.example.recipegpt.activities

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.recipegpt.adapters.RecipeAdapter
import com.example.recipegpt.databinding.ActivityHomeBinding
import com.example.recipegpt.models.Recipe
import com.example.recipegpt.services.RecipeService
import com.example.recipegpt.workers.RandomQuoteWorker
import java.util.concurrent.TimeUnit

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var recipeAdapter: RecipeAdapter
    private var recipeService: RecipeService? = null
    private var isBound = false
    private var searching = false
    private  val UNIQUE_WORK_NAME = "RandomQuoteWorker"
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
            binding.searchProgressBar.visibility = android.view.View.INVISIBLE
            searching = false
            Log.d("HomeActivity", "Service disconnected")
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(this, "Notification permission is required for fetching random quotes", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Schedule Random Quote Fetching Worker when launching app
        scheduleQuoteWorker(this)



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

                if(!searching){

                    if (query.isNotEmpty()) {
                        Log.d("Recipe Search Bar", "Query: $query")
                        searching = true
                        binding.searchProgressBar.visibility = android.view.View.VISIBLE
                        searchRecipes(query)
                    } else {
                        searching = false
                        binding.searchProgressBar.visibility = android.view.View.INVISIBLE

                        Toast.makeText(this, "Enter a search query", Toast.LENGTH_SHORT).show()
                    }
                }

                true
            } else {
                false
            }
        }
    }

    private fun scheduleQuoteWorker(context: Context) {
        // Define constraints for the worker
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Only run when network is connected
            .build()

        // Create a periodic work request
        val periodicWorkRequest = PeriodicWorkRequestBuilder<RandomQuoteWorker>(10, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .build()

        // Enqueue the work request
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "RandomQuoteWorker", // Unique name to prevent duplicate workers
            androidx.work.ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            periodicWorkRequest
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        // Unbind the service when the activity is destroyed
        if (isBound) {
            unbindService(serviceConnection)
            binding.searchProgressBar.visibility = android.view.View.INVISIBLE
            searching = false
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
            searching = false
            binding.searchProgressBar.visibility = android.view.View.INVISIBLE

        }
    }

    private fun displaySearchResults(results: List<Recipe>) {
        searching = false
        binding.searchProgressBar.visibility = android.view.View.INVISIBLE

        if (results.isNotEmpty()) {

            binding.noRecipesTextView.visibility = android.view.View.INVISIBLE

            binding.recipeRecyclerView.visibility = android.view.View.VISIBLE
            recipeAdapter.submitList(results)

        } else {
            binding.noRecipesTextView.visibility = android.view.View.VISIBLE
            binding.recipeRecyclerView.visibility = android.view.View.INVISIBLE
            Toast.makeText(this, "No recipes found", Toast.LENGTH_SHORT).show()
        }
    }
}