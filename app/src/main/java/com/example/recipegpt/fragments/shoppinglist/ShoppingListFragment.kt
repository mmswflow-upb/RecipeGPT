package com.example.recipegpt.fragments.shoppinglist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recipegpt.R
import com.example.recipegpt.adapters.ShoppingListIngredientAdapter
import com.example.recipegpt.databinding.FragmentShoppingListBinding
import com.example.recipegpt.models.Ingredient
import com.example.recipegpt.models.QuantUnit

class ShoppingListFragment : Fragment() {

    private var _binding: FragmentShoppingListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ShoppingListViewModel by viewModels()
    private lateinit var ingredientAdapter: ShoppingListIngredientAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchBar()
        setupObservers()
        setupPopup()

        // Handle clicking outside the popup
        binding.overlayBackground.setOnClickListener {
            viewModel.closePopup()
        }
    }

    private fun setupRecyclerView() {
        ingredientAdapter = ShoppingListIngredientAdapter(requireContext()) { ingredient ->
            viewModel.openPopupForIngredient(ingredient)
        }

        binding.shoppingListRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ingredientAdapter
        }
    }

    private fun setupObservers() {

        viewModel.filteredShoppingList.observe(viewLifecycleOwner) { ingredients ->
            Log.d("shopping list observer", "shopping list changed")
            ingredientAdapter.submitList(ingredients)
            binding.emptyShoppingListTextView.visibility =
                if (ingredients.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.popupIngredient.observe(viewLifecycleOwner) { ingredient ->
            if (ingredient != null) {
                showPopup(ingredient)
            } else {
                hidePopup()
            }
        }
        viewModel.popupSelectedUnit.observe(viewLifecycleOwner) { selectedUnit ->
            val unitIndex = QuantUnit.entries.indexOf(selectedUnit)
            if (unitIndex != -1) {
                binding.ingredientUnitSpinner.setSelection(unitIndex)
            }
        }


        viewModel.query.observe(viewLifecycleOwner) { query ->
            Log.d("query observer", "Query changed!")
            // Only set the text if it differs from what's currently there
            if (binding.searchShoppingList.text.toString() != query) {
                binding.searchShoppingList.setText(query)
            }
        }



    }

    private fun setupSearchBar() {
        // Set up the TextWatcher for live filtering
        binding.searchShoppingList.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("setupSearchBar-onTextChanged", "Text is changing")

                val query = s.toString().trim()
                viewModel.updateQuery(query) // Update the query in the ViewModel
                viewModel.applyQueryFilter() // Apply the filter
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Restore the query in the search bar if it exists
        binding.searchShoppingList.setText(viewModel.query.value)
    }



    private fun setupPopup() {
        val units = QuantUnit.entries.map { it.unit }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.ingredientUnitSpinner.adapter = adapter

        binding.ingredientUnitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedUnit = QuantUnit.entries[position]
                viewModel.updatePopupSelectedUnit(selectedUnit)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.popupDoneButton.setOnClickListener {
            val amount = binding.ingredientAmountInput.text.toString().toDoubleOrNull()
            val ingredient = viewModel.popupIngredient.value
            val selectedUnit = viewModel.popupSelectedUnit.value
            if (amount != null && ingredient != null && selectedUnit != null) {
                val updatedIngredient = ingredient.copy(
                    amount = amount,
                    unit = selectedUnit.unit
                )
                viewModel.addToDatabase(updatedIngredient)
                viewModel.closePopup()
            }
        }
    }

    private fun showPopup(ingredient: Ingredient) {
        binding.ingredientPopupCard.visibility = View.VISIBLE
        binding.overlayBackground.visibility = View.VISIBLE
        binding.popupTitle.text = getString(R.string.ingredient_popup_title, ingredient.item)
        binding.ingredientAmountInput.setText(ingredient.amount.toString())

        val unitIndex = QuantUnit.entries.indexOfFirst { it.unit == ingredient.unit }
        binding.ingredientUnitSpinner.setSelection(if (unitIndex != -1) unitIndex else 0)
    }

    private fun hidePopup() {
        binding.ingredientPopupCard.visibility = View.GONE
        binding.overlayBackground.visibility = View.GONE
        binding.ingredientAmountInput.text?.clear()
        binding.ingredientUnitSpinner.setSelection(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Converts unit to display-friendly format
    //Normalize the strings for units
    private fun convertUnitDisplay(unit: String): String {

        return when (unit) {
            QuantUnit.tablespoons_solids_plants_powders.unit -> {
                getString(R.string.tablespoons)
            }
            QuantUnit.teaspoons_solids_plants_powders.unit -> {
                getString(R.string.teaspoons)
            }
            QuantUnit.piece_about_50_grams.unit -> {
                getString(R.string.piece_about_50_grams)
            }
            QuantUnit.piece_about_100_grams.unit -> {
                getString(R.string.piece_about_100_grams)
            }
            QuantUnit.piece_about_250_grams.unit -> {
                getString(R.string.piece_about_250_grams)
            }
            QuantUnit.whole_pieces.unit -> {
                getString(R.string.whole_pieces)
            }
            else -> {
                unit
            }
        }
    }
}
