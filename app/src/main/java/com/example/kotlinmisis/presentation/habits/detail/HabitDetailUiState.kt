package com.example.kotlinmisis.presentation.habits.detail

data class HabitDetailUiState(
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val title: String = "",
    val description: String = "",
    val frequencyLabel: String = "",
    val colorHex: String = "#6750A4",
    val statusLabel: String = "",
    val currentStreakLabel: String = "",
    val bestStreakLabel: String = "",
    val completionHistory: List<String> = emptyList(),
    val syncLabel: String = "",
    val actionLabel: String = "",
    val completedToday: Boolean = false
)
