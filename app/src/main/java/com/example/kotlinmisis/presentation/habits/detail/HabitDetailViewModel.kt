package com.example.kotlinmisis.presentation.habits.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kotlinmisis.domain.model.Habit
import com.example.kotlinmisis.domain.model.HabitFrequency
import com.example.kotlinmisis.domain.usecase.DeleteHabitUseCase
import com.example.kotlinmisis.domain.usecase.ObserveHabitDetailUseCase
import com.example.kotlinmisis.domain.usecase.ToggleHabitCompletionUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HabitDetailViewModel(
    private val habitId: String,
    private val observeHabitDetailUseCase: ObserveHabitDetailUseCase,
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HabitDetailUiEvent>()
    val events: SharedFlow<HabitDetailUiEvent> = _events.asSharedFlow()

    init {
        observeDetail()
    }

    fun onToggleCompletion() {
        viewModelScope.launch {
            runCatching { toggleHabitCompletionUseCase(habitId) }
                .onFailure { _events.emit(HabitDetailUiEvent.ShowMessage("Failed to update.")) }
        }
    }

    fun onDeleteConfirmed() {
        viewModelScope.launch {
            runCatching { deleteHabitUseCase(habitId) }
                .onSuccess { _events.emit(HabitDetailUiEvent.HabitDeleted) }
                .onFailure { _events.emit(HabitDetailUiEvent.ShowMessage("Failed to delete.")) }
        }
    }

    private fun observeDetail() {
        viewModelScope.launch {
            observeHabitDetailUseCase(habitId).collect { habit ->
                if (habit == null) {
                    _uiState.update { it.copy(isLoading = false, notFound = true) }
                } else {
                    _uiState.update { toUiState(habit) }
                }
            }
        }
    }

    private fun toUiState(habit: Habit): HabitDetailUiState {
        val frequencyLabel = when (habit.frequency) {
            HabitFrequency.DAILY -> "Daily"
            HabitFrequency.WEEKLY -> "Weekly"
        }

        return HabitDetailUiState(
            isLoading = false,
            notFound = false,
            title = habit.title,
            description = habit.description,
            frequencyLabel = frequencyLabel,
            colorHex = habit.colorHex,
            statusLabel = if (habit.completedToday) "Completed today" else "Not completed today",
            currentStreakLabel = "${habit.currentStreak} day${if (habit.currentStreak != 1) "s" else ""}",
            bestStreakLabel = "${habit.bestStreak} day${if (habit.bestStreak != 1) "s" else ""}",
            completionHistory = habit.completionDates.take(30),
            syncLabel = if (habit.pendingSync) "Pending sync" else "Synced",
            actionLabel = if (habit.completedToday) "Undo today" else "Complete today",
            completedToday = habit.completedToday
        )
    }

    class Factory(
        private val habitId: String,
        private val observeHabitDetailUseCase: ObserveHabitDetailUseCase,
        private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase,
        private val deleteHabitUseCase: DeleteHabitUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HabitDetailViewModel(
                habitId, observeHabitDetailUseCase, toggleHabitCompletionUseCase, deleteHabitUseCase
            ) as T
        }
    }
}
