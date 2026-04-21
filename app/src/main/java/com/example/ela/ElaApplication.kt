package com.example.ela

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ElaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
