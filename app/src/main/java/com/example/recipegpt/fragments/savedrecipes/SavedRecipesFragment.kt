package com.example.recipegpt.fragments.savedrecipes

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recipegpt.adapters.RecipeAdapter
import com.example.recipegpt.databinding.FragmentSavedRecipesBinding

class SavedRecipesFragment : Fragment() {

    private var _binding: FragmentSavedRecipesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SavedRecipesViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        setupInputListeners()

        setupObservers()
    }

    private fun setupRecyclerView() {
        // Initialize RecyclerView and Adapter
        recipeAdapter = RecipeAdapter(requireContext())
        binding.savedRecipesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recipeAdapter
        }
    }
    private fun setupObservers(){
        viewModel.savedRecipes.observe(viewLifecycleOwner) {
            viewModel.filterRecipes(viewModel.query.value ?: "")
        }

        // Observe filtered recipes LiveData
        viewModel.filteredRecipes.observe(viewLifecycleOwner) { recipes ->
            recipeAdapter.submitList(recipes)
            // Display the No Recipes message when the list is empty
            if (recipes.isNotEmpty()) {
                binding.noSavedRecipesTextView.visibility = View.INVISIBLE
            } else {
                binding.noSavedRecipesTextView.visibility = View.VISIBLE
            }
        }
    }

    private fun setupInputListeners(){
        // Set up the TextWatcher for live filtering
        binding.searchSavedRecipes.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                viewModel.updateQuery(query)
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.searchSavedRecipes.setOnEditorActionListener{ v, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                // Clear focus from EditText
                binding.searchSavedRecipes.clearFocus()
                true
            } else {
                false
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

