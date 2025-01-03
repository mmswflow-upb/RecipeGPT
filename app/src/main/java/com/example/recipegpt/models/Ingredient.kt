package com.example.recipegpt.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Ingredient(
    @SerializedName("item") val item: String,
    @SerializedName("amount") val amount: Number,
    @SerializedName("unit") val unit: String
) : Parcelable