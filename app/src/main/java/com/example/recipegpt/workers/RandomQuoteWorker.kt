package com.example.recipegpt.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.recipegpt.R
import com.example.recipegpt.network.ApiClient
import com.example.recipegpt.network.ApiInterface
import com.example.recipegpt.utils.NotificationUtils
import retrofit2.HttpException
import java.net.SocketTimeoutException

class RandomQuoteWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val randomQuoteNotificationId = 3

    override fun doWork(): Result {
        // Calculate timeout for the quote request (use default for simplicity)
        val timeout = calculateTimeoutForQuotes()

        // Fetch the quote
        try {
            val api = ApiClient.getInstance(timeout).create(ApiInterface::class.java)
            val response = api.randomQuote().execute()

            if (response.isSuccessful && response.body() != null) {
                val quote = response.body()!!.quote
                // Show the quote as a notification
                NotificationUtils.showStandardNotification(
                    applicationContext,
                    applicationContext.getString(R.string.cooking_quote_notification_title),
                    quote,
                    randomQuoteNotificationId
                )
            } else {
                handleErrorNotification(
                    applicationContext.getString(R.string.failed_to_fetch_random_quote)
                )
            }
        } catch (e: SocketTimeoutException) {
            handleErrorNotification(
                applicationContext.getString(R.string.network_error, e.message)
            )
        } catch (e: HttpException) {
            handleErrorNotification(
                applicationContext.getString(R.string.network_error, e.message)
            )
        } catch (e: Exception) {
            handleErrorNotification(
                applicationContext.getString(R.string.unexpected_error, e.message)
            )
        }

        return Result.success()
    }

    private fun calculateTimeoutForQuotes(): Long {
        // Define a default timeout for fetching quotes (e.g., 10 seconds)
        return 10000L // 10 seconds
    }

    private fun handleErrorNotification(message: String) {
        NotificationUtils.showStandardNotification(
            applicationContext,
            applicationContext.getString(R.string.error),
            message,
            randomQuoteNotificationId
        )
    }
}
