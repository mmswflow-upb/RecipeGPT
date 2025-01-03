package com.example.recipegpt.fragments.savedingredients

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recipegpt.R
import com.example.recipegpt.adapters.SavedIngredientAdapter
import com.example.recipegpt.databinding.FragmentSavedIngredientsBinding
import com.example.recipegpt.models.Ingredient
import com.example.recipegpt.models.QuantUnit
import com.example.recipegpt.models.UnitConverter

class SavedIngredientsFragment : Fragment(R.layout.fragment_saved_ingredients) {
    private var _binding: FragmentSavedIngredientsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SavedIngredientsViewModel by viewModels()
    private lateinit var ingredientAdapter: SavedIngredientAdapter

    private lateinit var unitDisplayArray: Array<String> // Array for display units from resources

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedIngredientsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        unitDisplayArray = resources.getStringArray(R.array.quantity_units) // Load display names for units

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
        ingredientAdapter = SavedIngredientAdapter(requireContext(),
            onDeleteButtonClicked = {

                ingredient -> viewModel.deleteIngredient(ingredient)

            },
            onEditButtonClicked = {

                    ingredient -> viewModel.openPopupForIngredient(ingredient)

        })

        binding.savedIngredientsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ingredientAdapter
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupObservers() {
        // Observe filtered shopping list
        viewModel.filteredIngredientsList.observe(viewLifecycleOwner) { ingredients ->
            ingredientAdapter.submitList(ingredients)

            // Show or hide the empty list message
            binding.emptyIngredientsListTextView.visibility =
                if (ingredients.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe all shopping list items and trigger filtering when the data changes
        viewModel.ingredientsList.observe(viewLifecycleOwner) {
            viewModel.applyQueryFilter()
        }

        viewModel.popupIngredient.observe(viewLifecycleOwner) { ingredient ->
            if (ingredient != null) {
                // Determine the relevant units based on the ingredient's unit category
                val unitCategory = UnitConverter.getUnitCategory(QuantUnit.valueOf(ingredient.unit))
                val relevantUnits = UnitConverter.unitProgression[unitCategory].orEmpty()

                // Populate the spinner with relevant units
                val displayNames = relevantUnits.map {
                    UnitConverter.getDisplayName(requireContext(), it)
                }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, displayNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.savedIngredientUnitSpinner.adapter = adapter

                // Safely set the spinner's selected item to the ingredient's current unit
                val currentUnitIndex = relevantUnits.indexOfFirst { it == QuantUnit.valueOf(ingredient.unit) }

                if (currentUnitIndex != -1) {

                    binding.savedIngredientUnitSpinner.setSelection(currentUnitIndex)
                } else {
                    // Handle case where unit is not found in relevantUnits
                    binding.savedIngredientUnitSpinner.setSelection(0) // Default to the first item
                }

                // Show the popup
                showPopup()


            } else {
                hidePopup()
            }
        }

        viewModel.popupSelectedUnit.observe(viewLifecycleOwner) { newUnit ->
            val ingredient = viewModel.popupIngredient.value ?: return@observe

            if (newUnit?.unit != null) {
                // Retrieve the relevant units from the adapter
                val unitCategory = UnitConverter.getUnitCategory(QuantUnit.valueOf(ingredient.unit))
                val relevantUnits = UnitConverter.unitProgression[unitCategory]?.map {
                    UnitConverter.getDisplayName(requireContext(), it)
                } ?: emptyList()

                // Find the position of the new unit in the adapter
                val displayName = UnitConverter.getDisplayName(requireContext(), newUnit)
                val unitIndex = relevantUnits.indexOf(displayName)

                if (unitIndex != -1) {
                    // Convert the amount to the new unit
                    val convertedAmount = UnitConverter.convert(
                        ingredient.amount.toDouble(),
                        QuantUnit.valueOf(ingredient.unit),
                        newUnit
                    )

                    // Update the spinner selection using the adapter index
                    binding.savedIngredientUnitSpinner.setSelection(unitIndex)

                    // Update the displayed amount
                    binding.savedIngredientAmountInput.setText(convertedAmount.toString())
                } else {
                    // Fallback if the unit is not found
                    binding.savedIngredientUnitSpinner.setSelection(0)
                }
            }
        }

    }

    private fun setupSearchBar() {
        // Set up the TextWatcher for live filtering
        binding.searchSavedIngredients.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                viewModel.updateQuery(query)
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Handle the IME action to hide the keyboard
        binding.searchSavedIngredients.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                binding.searchSavedIngredients.clearFocus()
                true
            } else {
                false
            }
        }
    }

    private fun setupPopup() {

        binding.savedIngredientUnitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedDisplayName = parent?.getItemAtPosition(position).toString()
                val selectedUnit = UnitConverter.fromDisplayName(requireContext(), selectedDisplayName)
                viewModel.updatePopupSelectedUnit(selectedUnit)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.popupDoneButton.setOnClickListener {
            val amount = binding.savedIngredientAmountInput.text.toString().toDoubleOrNull()
            val ingredient = viewModel.popupIngredient.value
            val selectedUnit = viewModel.popupSelectedUnit.value
            if (amount != null && ingredient != null && selectedUnit != null) {

                if(amount > 0.0){

                    val updatedIngredient : Ingredient
                    if(selectedUnit.unit != ingredient.unit){
                        viewModel.deleteIngredient(ingredient)
                        updatedIngredient = ingredient.copy(
                            amount = amount,
                            unit = selectedUnit.unit
                        )
                    }else{
                        updatedIngredient = ingredient.copy(
                            amount = amount - ingredient.amount.toDouble(),
                            unit = selectedUnit.unit
                        )
                    }


                    viewModel.editIngredientInDatabase(updatedIngredient)
                    viewModel.closePopup()
                }else{
                    Toast.makeText(context, getString(R.string.invalid_amount_warning), Toast.LENGTH_SHORT).show()
                }

            }else{
                Toast.makeText(context, getString(R.string.invalid_input_ingredient_popup), Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun showPopup() {
        val ingredient = viewModel.popupIngredient.value!!
        binding.ingredientPopupCard.visibility = View.VISIBLE
        binding.overlayBackground.visibility = View.VISIBLE
        binding.popupTitle.text = getString(R.string.saved_ingredient_popup_title, ingredient.item)
        binding.savedIngredientAmountInput.setText(ingredient.amount.toString())


    }

    private fun hidePopup() {
        binding.ingredientPopupCard.visibility = View.GONE
        binding.overlayBackground.visibility = View.GONE
        binding.savedIngredientAmountInput.text?.clear()
        binding.savedIngredientUnitSpinner.setSelection(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}