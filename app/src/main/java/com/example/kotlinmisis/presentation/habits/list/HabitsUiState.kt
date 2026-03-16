package com.example.kotlinmisis.presentation.habits.list

data class HabitsUiState(
    val isLoading: Boolean = true,
    val habits: List<HabitListItemUiModel> = emptyList(),
    val summaryText: String = "",
    val emptyStateVisible: Boolean = false,
    val selectedFilter: HabitFilter = HabitFilter.ALL
)

enum class HabitFilter { ALL, ACTIVE, COMPLETED }
