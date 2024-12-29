package com.example.recipegpt.network

import com.example.recipegpt.BuildConfig
import com.example.recipegpt.models.EnumConverterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = BuildConfig.BASE_URL

    // Function to create OkHttpClient with a dynamic timeout
    private fun createHttpClient(timeout: Long): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.MILLISECONDS) // Connection timeout
            .readTimeout(timeout, TimeUnit.MILLISECONDS)   // Read timeout
            .writeTimeout(timeout, TimeUnit.MILLISECONDS)  // Write timeout
            .addInterceptor { chain ->
                try {
                    chain.proceed(chain.request())
                } catch (e: SocketTimeoutException) {
                    // Log timeout or handle as needed
                    throw e // Re-throw or convert to a custom exception
                }
            }
            .build()
    }

    // Function to create Retrofit instance with dynamic timeout
    fun getInstance(timeout: Long): Retrofit {
        val client = createHttpClient(timeout)
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(EnumConverterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

