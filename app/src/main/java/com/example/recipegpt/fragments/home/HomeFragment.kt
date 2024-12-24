package com.example.recipegpt.fragments.home

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recipegpt.R
import com.example.recipegpt.adapters.RecipeAdapter
import com.example.recipegpt.databinding.FragmentHomeBinding
import com.example.recipegpt.viewmodels.HomeViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var recipeAdapter: RecipeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // Initialize RecyclerView
        recipeAdapter = RecipeAdapter()
        binding.recipeRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recipeAdapter
        }

        // Bind to RecipeService
        viewModel.bindService(requireContext())

        // Observe ViewModel LiveData
        setupObservers()

        // Handle search bar interaction
        binding.searchEditText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val query = v.text.toString().trim()
                if (query.isNotEmpty()) {
                    viewModel.updateQuery(query)
                    performSearch(query)
                } else {
                    Toast.makeText(requireContext(), "Enter a search query", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }
    }

    private fun setupObservers() {
        viewModel.query.observe(viewLifecycleOwner) { query ->
            binding.searchEditText.setText(query)
        }

        viewModel.isSearching.observe(viewLifecycleOwner) { isSearching ->
            binding.searchProgressBar.visibility = if (isSearching) View.VISIBLE else View.INVISIBLE
        }

        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            recipeAdapter.submitList(recipes)
            binding.noRecipesTextView.visibility = if (recipes.isEmpty()) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun performSearch(query: String) {
        viewModel.updateSearchingStatus(true)
        lifecycleScope.launch {
            viewModel.searchRecipes(query) { results ->
                viewModel.updateRecipes(results)
                viewModel.updateSearchingStatus(false)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.unbindService(requireContext())
        _binding = null
    }
}
