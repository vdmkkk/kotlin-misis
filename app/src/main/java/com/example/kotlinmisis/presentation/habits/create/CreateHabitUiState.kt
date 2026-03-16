package com.example.kotlinmisis.presentation.habits.create

import com.example.kotlinmisis.domain.model.HabitFrequency
import com.example.kotlinmisis.presentation.common.HABIT_COLOR_PALETTE

data class CreateHabitUiState(
    val title: String = "",
    val description: String = "",
    val selectedFrequency: HabitFrequency = HabitFrequency.DAILY,
    val selectedColorHex: String = HABIT_COLOR_PALETTE.first(),
    val colorOptions: List<String> = HABIT_COLOR_PALETTE,
    val titleError: String? = null,
    val isSaving: Boolean = false
)
