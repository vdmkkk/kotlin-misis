package com.example.kotlinmisis.data.mapper

import com.example.kotlinmisis.data.local.HabitEntity
import com.example.kotlinmisis.data.remote.dto.HabitDto
import com.example.kotlinmisis.domain.model.Habit
import com.example.kotlinmisis.domain.model.HabitFrequency
import java.time.LocalDate

fun HabitEntity.toDomain(
    completionDates: List<String>,
    today: String = LocalDate.now().toString()
): Habit {
    val frequency = runCatching { HabitFrequency.valueOf(frequency) }
        .getOrDefault(HabitFrequency.DAILY)

    val sortedDates = completionDates.sortedDescending()
    val streaks = computeStreaks(sortedDates, today)

    return Habit(
        id = id,
        title = title,
        description = description,
        frequency = frequency,
        colorHex = colorHex,
        createdAt = createdAt,
        lastCompletedDate = lastCompletedDate,
        completedToday = lastCompletedDate == today,
        currentStreak = streaks.first,
        bestStreak = streaks.second,
        completionDates = sortedDates
    )
}

fun HabitDto.toEntity(): HabitEntity = HabitEntity(
    id = id,
    title = title,
    description = description,
    frequency = frequency,
    colorHex = colorHex,
    createdAt = createdAt,
    lastCompletedDate = lastCompletedDate
)

private fun computeStreaks(
    sortedDatesDesc: List<String>,
    today: String
): Pair<Int, Int> {
    if (sortedDatesDesc.isEmpty()) return 0 to 0

    val dates = sortedDatesDesc.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
        .toSortedSet()
    if (dates.isEmpty()) return 0 to 0

    var currentStreak = 0
    var checkDate = LocalDate.parse(today)
    if (checkDate !in dates) {
        checkDate = checkDate.minusDays(1)
    }
    while (checkDate in dates) {
        currentStreak++
        checkDate = checkDate.minusDays(1)
    }

    var bestStreak = 0
    var runningStreak = 0
    var prevDate: LocalDate? = null
    for (date in dates) {
        if (prevDate != null && date == prevDate.plusDays(1)) {
            runningStreak++
        } else {
            runningStreak = 1
        }
        if (runningStreak > bestStreak) bestStreak = runningStreak
        prevDate = date
    }

    return currentStreak to bestStreak
}
