package com.example.kotlinmisis.domain.usecase

import com.example.kotlinmisis.domain.repository.HabitsRepository

class ObserveHabitDetailUseCase(
    private val repository: HabitsRepository
) {
    operator fun invoke(habitId: String) = repository.observeHabitDetail(habitId)
}
