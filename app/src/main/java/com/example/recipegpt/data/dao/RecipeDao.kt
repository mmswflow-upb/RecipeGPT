package com.example.recipegpt.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.recipegpt.data.entities.RecipeEntity

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipesSync(): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE title = :name")
    suspend fun getRecipeByNameSync(name: String): RecipeEntity?

    @Query("SELECT * FROM recipes WHERE listed= 1")
    suspend fun getListedRecipes(): List<RecipeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)



    @Query("DELETE FROM recipes WHERE title = :name")
    suspend fun deleteRecipeByName(name: String)
}


