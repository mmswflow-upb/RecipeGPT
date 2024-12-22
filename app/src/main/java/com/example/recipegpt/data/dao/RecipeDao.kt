package com.example.recipegpt.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.recipegpt.data.entities.RecipeEntity

@Dao
interface RecipeDao {

    /**
     * Insert a single recipe into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    /**
     * Insert multiple recipes into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<RecipeEntity>)

    /**
     * Retrieve all recipes from the database.
     */
    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipes(): List<RecipeEntity>

    /**
     * Delete all recipes from the database.
     */
    @Query("DELETE FROM recipes")
    suspend fun deleteAllRecipes()
}

