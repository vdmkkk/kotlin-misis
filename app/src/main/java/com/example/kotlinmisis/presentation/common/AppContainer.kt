package com.example.kotlinmisis.presentation.common

import android.content.Context
import androidx.room.Room
import com.example.kotlinmisis.data.local.HabitsDatabase
import com.example.kotlinmisis.data.remote.HabitsApi
import com.example.kotlinmisis.data.remote.MockHabitsInterceptor
import com.example.kotlinmisis.data.repository.HabitsRepositoryImpl
import com.example.kotlinmisis.domain.usecase.CreateHabitUseCase
import com.example.kotlinmisis.domain.usecase.DeleteHabitUseCase
import com.example.kotlinmisis.domain.usecase.ObserveHabitDetailUseCase
import com.example.kotlinmisis.domain.usecase.ObserveHabitsUseCase
import com.example.kotlinmisis.domain.usecase.SyncHabitsUseCase
import com.example.kotlinmisis.domain.usecase.ToggleHabitCompletionUseCase
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {
    companion object {
        // Toggle to switch between the in-process mock and the real FastAPI backend.
        // When USE_REAL_BACKEND is true, set BACKEND_URL to the machine running
        // docker compose (use 10.0.2.2 from the Android emulator to reach the host).
        private const val USE_REAL_BACKEND = false
        private const val BACKEND_URL = "http://10.0.2.2:8182/"
    }

    private val gson = Gson()

    private val database: HabitsDatabase = Room.databaseBuilder(
        context,
        HabitsDatabase::class.java,
        "habits.db"
    ).fallbackToDestructiveMigration().build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .apply {
            if (!USE_REAL_BACKEND) {
                addInterceptor(MockHabitsInterceptor(gson))
            }
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(if (USE_REAL_BACKEND) BACKEND_URL else "https://mock.habits/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val habitsApi: HabitsApi = retrofit.create(HabitsApi::class.java)

    private val habitsRepository = HabitsRepositoryImpl(
        habitsDao = database.habitsDao(),
        habitsApi = habitsApi
    )

    val observeHabitsUseCase = ObserveHabitsUseCase(habitsRepository)
    val observeHabitDetailUseCase = ObserveHabitDetailUseCase(habitsRepository)
    val createHabitUseCase = CreateHabitUseCase(habitsRepository)
    val toggleHabitCompletionUseCase = ToggleHabitCompletionUseCase(habitsRepository)
    val deleteHabitUseCase = DeleteHabitUseCase(habitsRepository)
    val syncHabitsUseCase = SyncHabitsUseCase(habitsRepository)
}
