package com.example.kotlinmisis.data.remote

import com.example.kotlinmisis.data.remote.dto.HabitDto
import com.example.kotlinmisis.data.remote.dto.SyncHabitsRequest
import com.example.kotlinmisis.data.remote.dto.SyncHabitsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface HabitsApi {
    @GET("habits")
    suspend fun getHabits(): List<HabitDto>

    @POST("habits/sync")
    suspend fun syncHabits(@Body request: SyncHabitsRequest): SyncHabitsResponse
}
