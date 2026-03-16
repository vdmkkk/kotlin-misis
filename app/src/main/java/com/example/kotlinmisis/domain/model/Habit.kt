package com.example.kotlinmisis.domain.model

data class Habit(
    val id: String,
    val title: String,
    val description: String,
    val frequency: HabitFrequency,
    val colorHex: String,
    val createdAt: Long,
    val lastCompletedDate: String?,
    val completedToday: Boolean,
    val currentStreak: Int,
    val bestStreak: Int,
    val completionDates: List<String>
)
