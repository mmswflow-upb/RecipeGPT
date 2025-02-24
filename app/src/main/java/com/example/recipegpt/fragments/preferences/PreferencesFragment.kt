package com.example.recipegpt.fragments.preferences

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.recipegpt.R
import com.example.recipegpt.databinding.FragmentPreferencesBinding

class PreferencesFragment : Fragment(R.layout.fragment_preferences) {

    private val settingsViewModel: PreferencesViewModel by activityViewModels()
    private var _binding: FragmentPreferencesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreferencesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()

    }

    private fun setupListeners(){
        // Add ActionListener for the maxResultsInput
        binding.maxResultsInput.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                // Clear focus from EditText
                binding.maxResultsInput.clearFocus()

                true // Consume the event
            } else {
                false // Pass the event to the next listener
            }
        }

        // Handle Save Button click
        binding.saveSettingsButton.setOnClickListener {
            val maxResults = binding.maxResultsInput.text.toString().toIntOrNull()
            val selectedFrequency = binding.randomQuoteFrequencySelector.selectedItem.toString()

            if (maxResults != null && maxResults > 0 && maxResults <= 20) {
                // Save both settings to the ViewModel
                settingsViewModel.updateMaxResults(maxResults)
                settingsViewModel.updateRandomQuoteFrequency(selectedFrequency)

                // Notify the user
                Toast.makeText(requireContext(), getString(R.string.saved_preferences), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), getString(R.string.preferences_error_message), Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setupObservers(){
        // Set initial values from the ViewModel
        settingsViewModel.randomQuoteFrequency.observe(viewLifecycleOwner) { frequency ->
            val position = resources.getStringArray(R.array.quote_fetching_frequencies).indexOf(frequency)
            if (position >= 0) binding.randomQuoteFrequencySelector.setSelection(position)
        }

        settingsViewModel.maxResults.observe(viewLifecycleOwner) { maxResults ->
            binding.maxResultsInput.setText(maxResults.toString())
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
