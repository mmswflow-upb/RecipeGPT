package com.example.recipegpt.network

import com.example.recipegpt.BuildConfig
import com.example.recipegpt.R
import com.example.recipegpt.models.Recipe
import com.example.recipegpt.models.RecipesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiInterface {
    @Headers("devkey: ${BuildConfig.API_KEY}")
    @GET("/getRecipes")
    fun searchRecipes(
        @Query("numberOfRecipes") numberOfRecipes : Int,
        @Query("recipeQuery") recipeQuery: String
    ): Call<RecipesResponse>
}