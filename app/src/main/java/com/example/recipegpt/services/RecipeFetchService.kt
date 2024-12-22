package com.example.recipegpt.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.recipegpt.models.RecipeResponse
import com.example.recipegpt.network.ApiClient
import com.example.recipegpt.network.ApiInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeFetchService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        fetchRecipes()
        return START_NOT_STICKY
    }

    private fun fetchRecipes() {
        val api = ApiClient.instance.create(ApiInterface::class.java)
        api.fetchRecipes().enqueue(object : Callback<RecipeResponse> {
            override fun onResponse(call: Call<RecipeResponse>, response: Response<RecipeResponse>) {
                if (response.isSuccessful) {
                    val recipes = response.body()?.recipes ?: emptyList()
                    // Save recipes to the database here.
                }
            }

            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
