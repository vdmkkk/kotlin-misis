package com.example.kotlinmisis.data.remote.dto

data class CreateHabitRequest(
    val id: String,
    val title: String,
    val description: String,
    val frequency: String,
    val colorHex: String,
    val createdAt: Long
)
