package com.example.kotlinmisis.domain.usecase

import com.example.kotlinmisis.domain.repository.HabitsRepository

class ObserveHabitsUseCase(
    private val repository: HabitsRepository
) {
    operator fun invoke() = repository.observeHabits()
}
