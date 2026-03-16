package com.example.kotlinmisis.domain.usecase

import com.example.kotlinmisis.domain.model.HabitFrequency
import com.example.kotlinmisis.domain.repository.HabitsRepository

class CreateHabitUseCase(
    private val repository: HabitsRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String,
        frequency: HabitFrequency,
        colorHex: String
    ) {
        repository.createHabit(title, description, frequency, colorHex)
    }
}
