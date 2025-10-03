package com.example.g_kash


import android.app.Application
import com.example.g_kash.authentication.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class GKashApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@GKashApplication)
            modules(appModule)
        }
    }
}

