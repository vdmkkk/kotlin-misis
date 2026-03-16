package com.example.kotlinmisis.domain.usecase

import com.example.kotlinmisis.domain.repository.HabitsRepository

class SyncHabitsUseCase(
    private val repository: HabitsRepository
) {
    suspend operator fun invoke() {
        repository.syncHabits()
    }
}
