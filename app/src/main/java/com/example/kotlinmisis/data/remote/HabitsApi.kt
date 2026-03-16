package com.example.kotlinmisis.data.remote

import com.example.kotlinmisis.data.remote.dto.CreateHabitRequest
import com.example.kotlinmisis.data.remote.dto.HabitDto
import com.example.kotlinmisis.data.remote.dto.UpdateHabitRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface HabitsApi {
    @GET("habits")
    suspend fun getHabits(): List<HabitDto>

    @GET("habits/{id}")
    suspend fun getHabit(@Path("id") id: String): HabitDto

    @POST("habits")
    suspend fun createHabit(@Body request: CreateHabitRequest): HabitDto

    @PUT("habits/{id}")
    suspend fun updateHabit(@Path("id") id: String, @Body request: UpdateHabitRequest): HabitDto

    @DELETE("habits/{id}")
    suspend fun deleteHabit(@Path("id") id: String)
}
