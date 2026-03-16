package com.example.kotlinmisis.presentation.habits.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kotlinmisis.domain.model.HabitFrequency
import com.example.kotlinmisis.domain.usecase.CreateHabitUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateHabitViewModel(
    private val createHabitUseCase: CreateHabitUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateHabitUiState())
    val uiState: StateFlow<CreateHabitUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CreateHabitUiEvent>()
    val events: SharedFlow<CreateHabitUiEvent> = _events.asSharedFlow()

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title, titleError = null) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onFrequencySelected(position: Int) {
        val frequency = HabitFrequency.entries[position]
        _uiState.update { it.copy(selectedFrequency = frequency) }
    }

    fun onColorSelected(colorHex: String) {
        _uiState.update { it.copy(selectedColorHex = colorHex) }
    }

    fun onSaveClicked() {
        val currentState = _uiState.value
        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            runCatching {
                createHabitUseCase(
                    title = currentState.title,
                    description = currentState.description,
                    frequency = currentState.selectedFrequency,
                    colorHex = currentState.selectedColorHex
                )
                _events.emit(CreateHabitUiEvent.CloseAfterSave)
            }.onFailure { throwable ->
                _events.emit(
                    CreateHabitUiEvent.ShowMessage(
                        throwable.message ?: "Unable to save habit."
                    )
                )
            }

            _uiState.update { it.copy(isSaving = false) }
        }
    }

    class Factory(
        private val createHabitUseCase: CreateHabitUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CreateHabitViewModel(createHabitUseCase) as T
        }
    }
}
