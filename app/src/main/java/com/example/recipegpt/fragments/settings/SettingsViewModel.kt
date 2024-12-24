package com.example.recipegpt.fragments.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.recipegpt.utils.SharedPreferencesManager

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferencesManager = SharedPreferencesManager(application)

    private val _randomQuoteFrequency = MutableLiveData<String>()
    val randomQuoteFrequency: LiveData<String> get() = _randomQuoteFrequency

    private val _maxResults = MutableLiveData<Int>()
    val maxResults: LiveData<Int> get() = _maxResults

    init {
        // Load initial values from SharedPreferences
        _randomQuoteFrequency.value = sharedPreferencesManager.getRandomQuoteFrequency()
        _maxResults.value = sharedPreferencesManager.getMaxResults()
    }

    fun updateRandomQuoteFrequency(frequency: String) {
        _randomQuoteFrequency.value = frequency
        sharedPreferencesManager.saveRandomQuoteFrequency(frequency)
    }

    fun updateMaxResults(maxResults: Int) {
        _maxResults.value = maxResults
        sharedPreferencesManager.saveMaxResults(maxResults)
    }
}
