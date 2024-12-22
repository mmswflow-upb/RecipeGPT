package com.example.recipegpt.network

import com.example.recipegpt.BuildConfig
import com.example.recipegpt.data.converters.EnumConverterFactory
import okhttp3.OkHttpClient

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = BuildConfig.BASE_URL

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Connection timeout
        .readTimeout(30, TimeUnit.SECONDS)   // Read timeout
        .writeTimeout(30, TimeUnit.SECONDS)  // Write timeout
        .build()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Set the customized OkHttpClient
            .addConverterFactory(EnumConverterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}