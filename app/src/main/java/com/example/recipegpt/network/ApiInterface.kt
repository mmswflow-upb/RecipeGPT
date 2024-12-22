package com.example.recipegpt.network

import com.example.recipegpt.models.RecipeResponse
import retrofit2.Call
import retrofit2.http.GET

interface ApiInterface {
    @GET("recipes")
    fun fetchRecipes(): Call<RecipeResponse>
}
