package com.example.recipegpt.fragments.shoppinglist

import android.os.Bundle
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
    private var selectedUnit: QuantUnit? = null

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
        setupObservers()
        setupPopup()

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
        viewModel.shoppingList.observe(viewLifecycleOwner) { ingredients ->
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
    }

    private fun setupPopup() {
        val units = QuantUnit.entries.map { it.unit }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.ingredientUnitSpinner.adapter = adapter

        binding.ingredientUnitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedUnit = QuantUnit.entries.toTypedArray()[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.popupDoneButton.setOnClickListener {
            val amount = binding.ingredientAmountInput.text.toString().toDoubleOrNull()
            val ingredient = viewModel.popupIngredient.value
            if (amount != null && ingredient != null && selectedUnit != null) {
                val updatedIngredient = ingredient.copy(
                    amount = amount,
                    unit = selectedUnit!!.unit
                )
                viewModel.addToDatabase(updatedIngredient)
                viewModel.closePopup()
            }
        }
    }

    private fun showPopup(ingredient: Ingredient) {
        binding.ingredientPopupCard.visibility = View.VISIBLE
        binding.popupTitle.text = getString(R.string.ingredient_popup_title, ingredient.item)
    }

    private fun hidePopup() {
        binding.ingredientPopupCard.visibility = View.GONE
        binding.ingredientAmountInput.text?.clear()
        binding.ingredientUnitSpinner.setSelection(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

