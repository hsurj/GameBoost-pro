package com.gameboost.pro

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GameBoostApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        lateinit var instance: GameBoostApplication
            private set
    }
}

