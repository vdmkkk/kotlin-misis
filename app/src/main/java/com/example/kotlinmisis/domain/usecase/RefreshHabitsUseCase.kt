package com.example.kotlinmisis.domain.usecase

import com.example.kotlinmisis.domain.repository.HabitsRepository

class RefreshHabitsUseCase(
    private val repository: HabitsRepository
) {
    suspend operator fun invoke() {
        repository.refreshHabits()
    }
}
