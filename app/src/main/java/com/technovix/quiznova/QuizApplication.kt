package com.technovix.quiznova

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QuizApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Logger (Timber)

    }
}