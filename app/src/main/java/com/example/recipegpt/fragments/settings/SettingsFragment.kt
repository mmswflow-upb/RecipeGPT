package com.example.recipegpt.fragments.settings

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.recipegpt.R
import com.google.android.material.button.MaterialButton

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val settingsViewModel: SettingsViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val frequencySpinner = view.findViewById<Spinner>(R.id.randomQuoteFrequencySelector)
        val maxResultsInput = view.findViewById<EditText>(R.id.maxResultsInput)
        val saveButton = view.findViewById<MaterialButton>(R.id.saveSettingsButton)

        // Set initial values from the ViewModel
        settingsViewModel.randomQuoteFrequency.observe(viewLifecycleOwner) { frequency ->
            val position = resources.getStringArray(R.array.quote_fetching_frequencies).indexOf(frequency)
            if (position >= 0) frequencySpinner.setSelection(position)
        }

        settingsViewModel.maxResults.observe(viewLifecycleOwner) { maxResults ->
            maxResultsInput.setText(maxResults.toString())
        }



        // Handle Save Button click
        saveButton.setOnClickListener {
            val maxResults = maxResultsInput.text.toString().toIntOrNull()
            val selectedFrequency = frequencySpinner.selectedItem.toString()

            if (maxResults != null && maxResults > 0 && maxResults <= 20) {
                // Save both settings to the ViewModel
                settingsViewModel.updateMaxResults(maxResults)
                settingsViewModel.updateRandomQuoteFrequency(selectedFrequency)

                // Notify the user
                Toast.makeText(requireContext(), "Settings saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please enter a valid maximum number of results.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
