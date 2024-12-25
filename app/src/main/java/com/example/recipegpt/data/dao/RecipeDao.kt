package com.example.recipegpt.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.recipegpt.data.entities.RecipeEntity
import com.example.recipegpt.models.Recipe

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): LiveData<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE title = :name")
    fun getRecipeByName(name: String): LiveData<RecipeEntity?>

    @Query("SELECT * FROM recipes WHERE title = :name")
    suspend fun getRecipeByNameSync(name: String): RecipeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE title = :name")
    suspend fun deleteRecipeByName(name: String)
}


