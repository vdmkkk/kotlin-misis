package com.example.kotlinmisis.data.local

import androidx.room.Entity

@Entity(
    tableName = "habit_completions",
    primaryKeys = ["habitId", "date"]
)
data class HabitCompletionEntity(
    val habitId: String,
    val date: String
)
