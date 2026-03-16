package com.example.kotlinmisis.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitsDao {
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun observeHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    suspend fun getHabitById(habitId: String): HabitEntity?

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    fun observeHabitById(habitId: String): Flow<HabitEntity?>

    @Query("SELECT * FROM habits WHERE pendingSync = 1")
    suspend fun getPendingSyncHabits(): List<HabitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(habits: List<HabitEntity>)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: String)

    @Query("DELETE FROM habits")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(habits: List<HabitEntity>) {
        clearAll()
        upsertAll(habits)
    }

    // --- Completions ---

    @Query("SELECT date FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun getCompletionDates(habitId: String): List<String>

    @Query("SELECT date FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    fun observeCompletionDates(habitId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(completion: HabitCompletionEntity)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCompletion(habitId: String, date: String)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId")
    suspend fun deleteCompletionsForHabit(habitId: String)

    @Query("SELECT DISTINCT habitId, date FROM habit_completions")
    suspend fun getAllCompletions(): List<HabitCompletionEntity>
}
