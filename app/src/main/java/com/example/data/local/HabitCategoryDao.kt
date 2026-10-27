package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCategoryDao {

    @Query("SELECT * FROM habit_categories ORDER BY orderIndex ASC, name ASC")
    fun getAllCategories(): Flow<List<HabitCategoryEntity>>

    @Query("SELECT COUNT(*) FROM habit_categories")
    suspend fun getCategoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: HabitCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<HabitCategoryEntity>)

    @Update
    suspend fun updateCategory(category: HabitCategoryEntity)

    @Query("DELETE FROM habit_categories WHERE id = :id")
    suspend fun deleteCategoryById(id: String)
}
