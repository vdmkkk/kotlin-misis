package com.example.kotlinmisis

import android.app.Application
import com.example.kotlinmisis.presentation.common.AppContainer

class HabitsApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
