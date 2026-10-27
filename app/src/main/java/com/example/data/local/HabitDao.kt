package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the [HabitEntity] table.
 * Supports complete CRUD (Create, Read, Update, Delete) operations
 * for habit tracking, supporting both reactive Streams ([Flow]) and
 * one-shot suspend functions.
 */
@Dao
interface HabitDao {

    // --- CREATE ---

    /**
     * Inserts a single habit into the database. If a conflict occurs, replaces the existing record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    /**
     * Inserts a list of habits into the database. If a conflict occurs, replaces the existing records.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)

    // --- READ ---

    /**
     * Observes all habits ordered by creation date descending.
     */
    @Query("SELECT * FROM habits ORDER BY creationDate DESC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    /**
     * Fetches a habit by its unique ID (one-shot).
     */
    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: String): HabitEntity?

    /**
     * Observes a habit by its unique ID reactively.
     */
    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    fun observeHabitById(id: String): Flow<HabitEntity?>

    /**
     * Observes habits filtered by category ordered by creation date descending.
     */
    @Query("SELECT * FROM habits WHERE category = :category ORDER BY creationDate DESC")
    fun getHabitsByCategory(category: String): Flow<List<HabitEntity>>

    /**
     * Observes habits filtered by completion status ordered by creation date descending.
     */
    @Query("SELECT * FROM habits WHERE isCompleted = :isCompleted ORDER BY creationDate DESC")
    fun getHabitsByCompletion(isCompleted: Boolean): Flow<List<HabitEntity>>

    /**
     * Gets the total count of habits in the database.
     */
    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getHabitCount(): Int

    // --- UPDATE ---

    /**
     * Updates an existing habit.
     */
    @Update
    suspend fun updateHabit(habit: HabitEntity)

    /**
     * Updates multiple existing habits.
     */
    @Update
    suspend fun updateHabits(habits: List<HabitEntity>)

    /**
     * Updates only the completion status of a specific habit.
     */
    @Query("UPDATE habits SET isCompleted = :completed WHERE id = :id")
    suspend fun updateCompletion(id: String, completed: Boolean)

    // --- DELETE ---

    /**
     * Deletes a specific habit entity.
     */
    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    /**
     * Deletes multiple habit entities.
     */
    @Delete
    suspend fun deleteHabits(habits: List<HabitEntity>)

    /**
     * Deletes a habit by its unique ID.
     */
    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: String): Int

    /**
     * Deletes all habits from the database.
     */
    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits(): Int
}

