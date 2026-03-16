package com.example.kotlinmisis.presentation.habits.detail

sealed interface HabitDetailUiEvent {
    data class ShowMessage(val message: String) : HabitDetailUiEvent
    data object HabitDeleted : HabitDetailUiEvent
}
