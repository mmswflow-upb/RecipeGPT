package com.example.recipegpt.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.recipegpt.databinding.ActivityShoppingListBinding

class ShoppingListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShoppingListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShoppingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up any logic for the Shopping List here
    }
}
