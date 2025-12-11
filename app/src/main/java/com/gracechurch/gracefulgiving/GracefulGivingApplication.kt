package com.gracechurch.gracefulgiving

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GracefulGivingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Any app-level initialization can go here
    }
}