package com.example.recipegpt.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.recipegpt.data.entities.IngredientEntity
import com.example.recipegpt.models.Ingredient

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients")
    fun getAllIngredients(): LiveData<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE item = :name")
    fun getIngredientByName(name: String): LiveData<IngredientEntity?>

    @Query("SELECT * FROM ingredients WHERE item = :name")
    suspend fun getIngredientByNameSync(name: String): IngredientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: IngredientEntity)

    @Update
    suspend fun updateIngredient(ingredient: IngredientEntity)

    @Query("DELETE FROM ingredients WHERE item = :name")
    suspend fun deleteIngredientByName(name: String)
}
