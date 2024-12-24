package com.example.recipegpt.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.recipegpt.network.ApiClient
import com.example.recipegpt.network.ApiInterface
import com.example.recipegpt.utils.NotificationUtils
import retrofit2.HttpException

class RandomQuoteWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Fetch the quote
        try {
            val api = ApiClient.instance.create(ApiInterface::class.java)
            val response = api.randomQuote().execute()

            if (response.isSuccessful && response.body() != null) {
                val quote = response.body()!!.quote
                Log.d("RandomQuoteWorker", "Fetched random quote: $quote")
                // Show the quote as a notification
                NotificationUtils.showNotification(
                    applicationContext,
                    "Cooking Quote",
                    quote
                )
            } else {
                NotificationUtils.showNotification(
                    applicationContext,
                    "Error",
                    "Failed to fetch a random quote. Please try again later."
                )
            }
        } catch (e: HttpException) {
            NotificationUtils.showNotification(
                applicationContext,
                "Error",
                "Network error: ${e.message}"
            )
        } catch (e: Exception) {
            NotificationUtils.showNotification(
                applicationContext,
                "Error",
                "Unexpected error: ${e.message}"
            )
        }

        return Result.success()
    }
}
