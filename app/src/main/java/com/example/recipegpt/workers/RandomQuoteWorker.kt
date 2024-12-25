package com.example.recipegpt.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.recipegpt.R
import com.example.recipegpt.network.ApiClient
import com.example.recipegpt.network.ApiInterface
import com.example.recipegpt.utils.NotificationUtils
import retrofit2.HttpException

class RandomQuoteWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val randomQuoteNotificationId = 3
    override fun doWork(): Result {
        // Fetch the quote
        try {
            val api = ApiClient.instance.create(ApiInterface::class.java)
            val response = api.randomQuote().execute()

            if (response.isSuccessful && response.body() != null) {
                val quote = response.body()!!.quote
                Log.d("RandomQuoteWorker", "Fetched random quote: $quote")
                // Show the quote as a notification
                NotificationUtils.showStandardNotification(
                    applicationContext,
                    applicationContext.getString(R.string.cooking_quote_notification_title),
                    quote,
                    randomQuoteNotificationId


                )
            } else {
                NotificationUtils.showStandardNotification(
                    applicationContext,
                    applicationContext.getString(R.string.error),
                    applicationContext.getString(R.string.failed_to_fetch_random_quote),
                    randomQuoteNotificationId
                )
            }
        } catch (e: HttpException) {
            NotificationUtils.showStandardNotification(
                applicationContext,
                applicationContext.getString(R.string.error),
                applicationContext.getString(R.string.network_error, e.message),
                randomQuoteNotificationId
            )
        } catch (e: Exception) {
            NotificationUtils.showStandardNotification(
                applicationContext,
                applicationContext.getString(R.string.error),
                applicationContext.getString(R.string.unexpected_error, e.message),
                randomQuoteNotificationId
            )
        }

        return Result.success()
    }
}
