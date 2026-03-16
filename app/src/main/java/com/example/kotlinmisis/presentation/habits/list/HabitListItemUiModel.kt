package com.example.kotlinmisis.presentation.habits.list

data class HabitListItemUiModel(
    val id: String,
    val title: String,
    val description: String,
    val frequencyLabel: String,
    val statusLabel: String,
    val streakLabel: String,
    val actionLabel: String,
    val colorHex: String,
    val completedToday: Boolean
)
