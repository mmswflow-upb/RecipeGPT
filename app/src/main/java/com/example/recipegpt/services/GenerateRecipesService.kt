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

    // Inner class for the client Binder
    inner class LocalBinder : Binder() {
        fun getService(): GenerateRecipeService = this@GenerateRecipeService
    }

    // Binder instance to bind the service
    private val binder = LocalBinder()



    override fun onCreate() {
        super.onCreate()
        // Initialize the API service
        apiService = ApiClient.instance.create(ApiInterface::class.java)
    }

    fun generateRecipes(query: String, numberOfRecipes: Int, callback: (List<Recipe>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
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
