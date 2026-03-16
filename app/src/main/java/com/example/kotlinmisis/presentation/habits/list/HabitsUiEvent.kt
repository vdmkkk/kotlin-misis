package com.example.kotlinmisis.presentation.habits.list

sealed interface HabitsUiEvent {
    data object NavigateToCreateHabit : HabitsUiEvent
    data class NavigateToDetail(val habitId: String) : HabitsUiEvent
    data class ShowMessage(val message: String) : HabitsUiEvent
    data class ConfirmDelete(val habitId: String, val habitTitle: String) : HabitsUiEvent
}
