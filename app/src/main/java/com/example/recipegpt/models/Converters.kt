package com.example.recipegpt.models

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromIngredientList(ingredients: List<Ingredient>): String {
        return gson.toJson(ingredients)
    }

    @TypeConverter
    fun toIngredientList(data: String): List<Ingredient> {
        val listType = object : TypeToken<List<Ingredient>>() {}.type
        return gson.fromJson<List<Ingredient>>(data, listType).map {
            ingredient : Ingredient -> ingredient.copy(item= ingredient.item.lowercase())
        }
    }

    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toStringList(data: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(data, listType)
    }
}


class EnumConverterFactory : Converter.Factory() {
    override fun stringConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<*, String>? {
        if (type is Class<*> && type.isEnum) {
            return Converter<Enum<*>, String> { enum -> (enum as QuantUnit).unit }
        }
        return null
    }
}
