package com.example.kotlinmisis.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val frequency: String,
    val colorHex: String,
    val createdAt: Long,
    val lastCompletedDate: String?
)
