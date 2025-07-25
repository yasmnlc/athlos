package com.example.athlos.data.local

import androidx.room.*
import com.example.athlos.ui.models.FoodItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodEntryDao {
    @Query("SELECT * FROM food_entries WHERE date = :date")
    fun getFoodEntriesForDate(date: String): Flow<List<FoodItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodEntry(foodItem: FoodItem)

    @Update
    suspend fun updateFoodEntry(foodItem: FoodItem)

    @Delete
    suspend fun deleteFoodEntry(foodItem: FoodItem)

    @Query("DELETE FROM food_entries WHERE date < :yesterdayDate")
    suspend fun deleteAllOldEntries(yesterdayDate: String)
}