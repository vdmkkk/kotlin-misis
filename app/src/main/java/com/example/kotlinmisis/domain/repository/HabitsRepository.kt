package com.example.kotlinmisis.domain.repository

import com.example.kotlinmisis.domain.model.Habit
import com.example.kotlinmisis.domain.model.HabitFrequency
import kotlinx.coroutines.flow.Flow

interface HabitsRepository {
    fun observeHabits(): Flow<List<Habit>>

    fun observeHabitDetail(habitId: String): Flow<Habit?>

    suspend fun refreshHabits()

    suspend fun createHabit(
        title: String,
        description: String,
        frequency: HabitFrequency,
        colorHex: String
    )

    suspend fun toggleHabitCompletion(habitId: String)

    suspend fun deleteHabit(habitId: String)
}
