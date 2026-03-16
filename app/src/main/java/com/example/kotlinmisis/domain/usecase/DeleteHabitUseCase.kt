package com.example.kotlinmisis.domain.usecase

import com.example.kotlinmisis.domain.repository.HabitsRepository

class DeleteHabitUseCase(
    private val repository: HabitsRepository
) {
    suspend operator fun invoke(habitId: String) {
        repository.deleteHabit(habitId)
    }
}
