package com.example.kotlinmisis.presentation.habits.create

sealed interface CreateHabitUiEvent {
    data object CloseAfterSave : CreateHabitUiEvent
    data class ShowMessage(val message: String) : CreateHabitUiEvent
}
