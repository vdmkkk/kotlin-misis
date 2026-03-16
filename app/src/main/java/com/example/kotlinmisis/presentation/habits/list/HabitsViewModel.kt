package com.example.kotlinmisis.presentation.habits.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kotlinmisis.domain.model.Habit
import com.example.kotlinmisis.domain.model.HabitFrequency
import com.example.kotlinmisis.domain.usecase.DeleteHabitUseCase
import com.example.kotlinmisis.domain.usecase.ObserveHabitsUseCase
import com.example.kotlinmisis.domain.usecase.RefreshHabitsUseCase
import com.example.kotlinmisis.domain.usecase.ToggleHabitCompletionUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HabitsViewModel(
    private val observeHabitsUseCase: ObserveHabitsUseCase,
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val refreshHabitsUseCase: RefreshHabitsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitsUiState())
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HabitsUiEvent>()
    val events: SharedFlow<HabitsUiEvent> = _events.asSharedFlow()

    private var allHabits: List<Habit> = emptyList()

    init {
        observeHabits()
        refresh()
    }

    fun onResume() {
        refresh()
    }

    fun onAddHabitRequested() {
        viewModelScope.launch { _events.emit(HabitsUiEvent.NavigateToCreateHabit) }
    }

    fun onHabitClicked(habitId: String) {
        viewModelScope.launch { _events.emit(HabitsUiEvent.NavigateToDetail(habitId)) }
    }

    fun onHabitCompletionClicked(habitId: String) {
        viewModelScope.launch {
            runCatching { toggleHabitCompletionUseCase(habitId) }
                .onFailure { _events.emit(HabitsUiEvent.ShowMessage("Unable to update habit.")) }
        }
    }

    fun onSwipeToDelete(habitId: String) {
        val habit = allHabits.find { it.id == habitId }
        viewModelScope.launch {
            _events.emit(HabitsUiEvent.ConfirmDelete(habitId, habit?.title ?: "this habit"))
        }
    }

    fun onDeleteConfirmed(habitId: String) {
        viewModelScope.launch {
            runCatching { deleteHabitUseCase(habitId) }
                .onSuccess { _events.emit(HabitsUiEvent.ShowMessage("Habit deleted.")) }
                .onFailure { _events.emit(HabitsUiEvent.ShowMessage("Delete failed.")) }
        }
    }

    fun onFilterSelected(filter: HabitFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        applyFilter()
    }

    private fun refresh() {
        viewModelScope.launch {
            refreshHabitsUseCase()
        }
    }

    private fun observeHabits() {
        viewModelScope.launch {
            observeHabitsUseCase().collect { habits ->
                allHabits = habits
                applyFilter()
            }
        }
    }

    private fun applyFilter() {
        val filter = _uiState.value.selectedFilter
        val filtered = when (filter) {
            HabitFilter.ALL -> allHabits
            HabitFilter.ACTIVE -> allHabits.filter { !it.completedToday }
            HabitFilter.COMPLETED -> allHabits.filter { it.completedToday }
        }

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                habits = filtered.map(::toUiModel),
                summaryText = buildSummary(allHabits),
                emptyStateVisible = filtered.isEmpty()
            )
        }
    }

    private fun toUiModel(habit: Habit): HabitListItemUiModel {
        val frequencyLabel = when (habit.frequency) {
            HabitFrequency.DAILY -> "Daily"
            HabitFrequency.WEEKLY -> "Weekly"
        }

        return HabitListItemUiModel(
            id = habit.id,
            title = habit.title,
            description = habit.description,
            frequencyLabel = frequencyLabel,
            statusLabel = if (habit.completedToday) "Completed today" else "Not completed today",
            streakLabel = if (habit.currentStreak > 0) {
                "${habit.currentStreak}-day streak"
            } else {
                "No streak yet"
            },
            actionLabel = if (habit.completedToday) "Undo" else "Done",
            colorHex = habit.colorHex,
            completedToday = habit.completedToday
        )
    }

    private fun buildSummary(habits: List<Habit>): String {
        if (habits.isEmpty()) return "No habits yet. Add one to start tracking."
        val completedCount = habits.count { it.completedToday }
        return "$completedCount of ${habits.size} habits completed today"
    }

    class Factory(
        private val observeHabitsUseCase: ObserveHabitsUseCase,
        private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase,
        private val deleteHabitUseCase: DeleteHabitUseCase,
        private val refreshHabitsUseCase: RefreshHabitsUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HabitsViewModel(
                observeHabitsUseCase, toggleHabitCompletionUseCase,
                deleteHabitUseCase, refreshHabitsUseCase
            ) as T
        }
    }
}
