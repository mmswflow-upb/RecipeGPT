package com.example.recipegpt.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.recipegpt.R
import com.example.recipegpt.utils.NotificationUtils

class SearchNotificationForegroundService : Service() {

    private var startTime: Long = 0L
    private var currentQuery: String? = null
    private var isRunning = false
    private lateinit var updateThread: Thread

    // Unique IDs for notifications
    private val searchNotificationId = 1
    private val searchCompleteNotificationId = 2

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            "START" -> {
                currentQuery = intent.getStringExtra("query") ?: getString(R.string.unknown_query)
                startTime = System.currentTimeMillis()
                isRunning = true

                NotificationUtils.showStandardNotification(
                    this,
                    getString(R.string.recipes_search_title),
                    getString(R.string.search_starting_message),
                    searchNotificationId
                )

                // Start the persistent notification
                val notification = NotificationUtils.createPersistentNotification(
                    this,
                    getString(R.string.recipes_search_title),
                    getString(R.string.search_in_progress_message, currentQuery)
                )
                startForeground(searchNotificationId, notification)

                // Start the background thread for periodic updates
                updateThread = Thread {
                    while (isRunning) {
                        val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000
                        NotificationUtils.updatePersistentNotification(
                            this,
                            getString(R.string.recipes_search_title),
                            getString(R.string.search_elapsed_time_message, currentQuery, elapsedSeconds),
                            searchNotificationId
                        )
                        try {
                            Thread.sleep(1000) // Sleep for 1 second
                        } catch (e: InterruptedException) {
                            Thread.currentThread().interrupt()
                        }
                    }
                }
                updateThread.start()
            }
            "STOP" -> {
                isRunning = false
                val elapsedTime = (System.currentTimeMillis() - startTime) / 1000
                val resultMessage = getString(R.string.search_complete_message, elapsedTime, currentQuery)

                // Show final notification
                NotificationUtils.showStandardNotification(
                    this,
                    getString(R.string.recipes_search_complete_title),
                    resultMessage,
                    searchCompleteNotificationId
                )

                // Stop the persistent notification and clean up
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        if (::updateThread.isInitialized && updateThread.isAlive) {
            updateThread.interrupt() // Interrupt the thread if still running
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
