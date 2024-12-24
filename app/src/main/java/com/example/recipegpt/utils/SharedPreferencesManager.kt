package com.example.recipegpt.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class SharedPreferencesManager(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_RANDOM_QUOTE_FREQUENCY = "random_quote_frequency"
        private const val KEY_MAX_RESULTS = "max_results"
        private const val DEFAULT_FREQUENCY = "15 minutes"
        private const val DEFAULT_MAX_RESULTS = 10

        const val ACTION_SETTINGS_UPDATED = "com.example.recipegpt.ACTION_SETTINGS_UPDATED"
        const val EXTRA_RANDOM_QUOTE_FREQUENCY = "extra_random_quote_frequency"
        const val EXTRA_MAX_RESULTS = "extra_max_results"
    }

    fun getRandomQuoteFrequency(): String {
        return sharedPreferences.getString(KEY_RANDOM_QUOTE_FREQUENCY, DEFAULT_FREQUENCY) ?: DEFAULT_FREQUENCY
    }

    fun saveRandomQuoteFrequency(frequency: String) {
        sharedPreferences.edit().putString(KEY_RANDOM_QUOTE_FREQUENCY, frequency).apply()
        broadcastSettingsUpdate()
    }

    fun getMaxResults(): Int {
        return sharedPreferences.getInt(KEY_MAX_RESULTS, DEFAULT_MAX_RESULTS)
    }

    fun saveMaxResults(maxResults: Int) {
        sharedPreferences.edit().putInt(KEY_MAX_RESULTS, maxResults).apply()
        broadcastSettingsUpdate()
    }

    private fun broadcastSettingsUpdate() {
        val intent = Intent(ACTION_SETTINGS_UPDATED).apply {
            putExtra(EXTRA_RANDOM_QUOTE_FREQUENCY, getRandomQuoteFrequency())
            putExtra(EXTRA_MAX_RESULTS, getMaxResults())
        }
        Log.d("SharedPreferencesManager-broadcastSettingsUpdate", "Broadcasting the new shared preferences: ${getMaxResults()} | ${getRandomQuoteFrequency()}")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}
