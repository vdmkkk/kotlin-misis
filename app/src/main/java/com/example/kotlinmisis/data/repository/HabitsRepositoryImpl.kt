package com.example.kotlinmisis.data.repository

import com.example.kotlinmisis.data.local.HabitCompletionEntity
import com.example.kotlinmisis.data.local.HabitEntity
import com.example.kotlinmisis.data.local.HabitsDao
import com.example.kotlinmisis.data.mapper.toDomain
import com.example.kotlinmisis.data.mapper.toDto
import com.example.kotlinmisis.data.mapper.toEntity
import com.example.kotlinmisis.data.remote.HabitsApi
import com.example.kotlinmisis.data.remote.dto.SyncHabitsRequest
import com.example.kotlinmisis.domain.model.Habit
import com.example.kotlinmisis.domain.model.HabitFrequency
import com.example.kotlinmisis.domain.repository.HabitsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.UUID

class HabitsRepositoryImpl(
    private val habitsDao: HabitsDao,
    private val habitsApi: HabitsApi
) : HabitsRepository {

    override fun observeHabits(): Flow<List<Habit>> {
        return habitsDao.observeHabits().map { habits ->
            val today = LocalDate.now().toString()
            val allCompletions = withContext(Dispatchers.IO) {
                habitsDao.getAllCompletions()
            }
            val completionsByHabit = allCompletions.groupBy(
                keySelector = { it.habitId },
                valueTransform = { it.date }
            )
            habits.map { entity ->
                entity.toDomain(
                    completionDates = completionsByHabit[entity.id].orEmpty(),
                    today = today
                )
            }
        }
    }

    override fun observeHabitDetail(habitId: String): Flow<Habit?> {
        return combine(
            habitsDao.observeHabitById(habitId),
            habitsDao.observeCompletionDates(habitId)
        ) { entity, completionDates ->
            val today = LocalDate.now().toString()
            entity?.toDomain(completionDates, today)
        }
    }

    override suspend fun createHabit(
        title: String,
        description: String,
        frequency: HabitFrequency,
        colorHex: String
    ) = withContext(Dispatchers.IO) {
        val habit = HabitEntity(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            description = description.trim(),
            frequency = frequency.name,
            colorHex = colorHex,
            createdAt = System.currentTimeMillis(),
            lastCompletedDate = null,
            pendingSync = true
        )
        habitsDao.upsert(habit)
    }

    override suspend fun toggleHabitCompletion(habitId: String) = withContext(Dispatchers.IO) {
        val habit = habitsDao.getHabitById(habitId) ?: return@withContext
        val today = LocalDate.now().toString()
        val wasCompletedToday = habit.lastCompletedDate == today

        if (wasCompletedToday) {
            habitsDao.deleteCompletion(habitId, today)
        } else {
            habitsDao.insertCompletion(HabitCompletionEntity(habitId, today))
        }

        habitsDao.upsert(
            habit.copy(
                lastCompletedDate = if (wasCompletedToday) null else today,
                pendingSync = true
            )
        )
    }

    override suspend fun deleteHabit(habitId: String) = withContext(Dispatchers.IO) {
        habitsDao.deleteCompletionsForHabit(habitId)
        habitsDao.deleteHabitById(habitId)
    }

    override suspend fun syncHabits() = withContext(Dispatchers.IO) {
        val pendingHabits = habitsDao.getPendingSyncHabits()
        if (pendingHabits.isNotEmpty()) {
            habitsApi.syncHabits(SyncHabitsRequest(pendingHabits.map { it.toDto() }))
        }

        val remoteHabits = habitsApi.getHabits()
        habitsDao.replaceAll(remoteHabits.map { it.toEntity() })
    }
}
