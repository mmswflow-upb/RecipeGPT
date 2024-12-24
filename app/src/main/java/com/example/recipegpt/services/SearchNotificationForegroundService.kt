package com.example.recipegpt.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.recipegpt.utils.NotificationUtils

class SearchNotificationForegroundService : Service() {

    private var startTime: Long = 0L
    private var currentQuery: String? = null
    private var isRunning = false
    private lateinit var updateThread: Thread

    // Unique IDs for notifications
    private val searchNotificationId = 1
    private val searchCompleteNotificationId = 2

    override fun onCreate() {
        super.onCreate()
        NotificationUtils.createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            "START" -> {
                currentQuery = intent.getStringExtra("query") ?: "unknown query"
                startTime = System.currentTimeMillis()
                isRunning = true

                // Start the persistent notification
                val notification = NotificationUtils.createPersistentNotification(
                    this,
                    "Recipes Search",
                    "Searching recipes for \"$currentQuery\"..."
                )
                startForeground(searchNotificationId, notification)

                // Start the background thread for periodic updates
                updateThread = Thread {
                    while (isRunning) {
                        val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000
                        NotificationUtils.updatePersistentNotification(
                            this,
                            "Recipes Search",
                            "Searching recipes for \"$currentQuery\"... ($elapsedSeconds seconds elapsed)",
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
                val resultMessage = "It took $elapsedTime seconds to find recipes for \"$currentQuery\""

                // Show final notification
                NotificationUtils.showStandardNotification(
                    this,
                    "Recipes Search Complete",
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
