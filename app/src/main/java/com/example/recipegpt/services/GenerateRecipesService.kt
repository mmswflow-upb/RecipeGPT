package com.example.recipegpt.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.recipegpt.R
import com.example.recipegpt.models.Recipe
import com.example.recipegpt.network.ApiClient
import com.example.recipegpt.network.ApiInterface
import com.example.recipegpt.utils.NotificationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException

class GenerateRecipeService : Service() {

    private var apiService: ApiInterface? = null

    inner class LocalBinder : Binder() {
        fun getService(): GenerateRecipeService = this@GenerateRecipeService
    }

    private val binder = LocalBinder()



    fun generateRecipes(query: String, numberOfRecipes: Int, callback: (List<Recipe>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            // Calculate timeout dynamically
            val timeout = calculateTimeout(numberOfRecipes)

            // Create Retrofit instance with dynamic timeout
            val retrofit = ApiClient.getInstance(timeout)
            apiService = retrofit.create(ApiInterface::class.java)

            val recipes = try {
                val response = apiService?.searchRecipes(numberOfRecipes, query)?.execute()
                if (response?.isSuccessful == true) {
                    response.body()?.recipes ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: SocketTimeoutException) {
                handleTimeoutException(e, query)
                emptyList()
            } catch (e: Exception) {
                handleGenericException(e)
                emptyList()
            }

            withContext(Dispatchers.Main) {
                callback(recipes)
            }
        }
    }

    private fun handleTimeoutException(e: SocketTimeoutException, query: String) {
        e.printStackTrace()
        val title = getString(R.string.timeout_error_title)
        val message = getString(R.string.timeout_error_message, query)
        NotificationUtils.showStandardNotification(
            this,
            title,
            message,
            notificationId = 4 // Unique ID for timeout notifications
        )
    }

    private fun calculateTimeout(numberOfRecipes: Int): Long {
        val baseTimeout = 5000L // 5 seconds
        val timeoutPerRecipe = 10000L // 10 second per recipe
        val maxTimeout =120000L // Cap at 2 minutes

        return (baseTimeout + (numberOfRecipes * timeoutPerRecipe)).coerceAtMost(maxTimeout)
    }


    private fun handleGenericException(e: Exception) {
        e.printStackTrace()
        val title = getString(R.string.generic_error_title)
        val message = getString(R.string.generic_error_message)
        NotificationUtils.showStandardNotification(
            this,
            title,
            message,
            notificationId = 5 // Unique ID for generic error notifications
        )
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}

