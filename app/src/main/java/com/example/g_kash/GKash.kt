package com.example.g_kash


import android.app.Application
import com.example.g_kash.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

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

