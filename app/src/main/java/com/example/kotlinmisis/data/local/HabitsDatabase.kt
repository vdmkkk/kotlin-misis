package com.example.kotlinmisis.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [HabitEntity::class, HabitCompletionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class HabitsDatabase : RoomDatabase() {
    abstract fun habitsDao(): HabitsDao
}
