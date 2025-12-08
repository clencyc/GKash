package com.example.g_kash

import android.app.Application
import com.example.g_kash.di.appModule
import com.example.g_kash.di.networkModule
import com.example.g_kash.di.profileModule
import com.example.g_kash.points.di.pointsModule
import com.example.g_kash.wallet.di.walletModule
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class GKashApplication : Application() {
    // Firebase Analytics instance
    lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        firebaseAnalytics = Firebase.analytics

        startKoin {
            androidLogger()
            androidContext(this@GKashApplication)
            modules(
                appModule,
                networkModule,
                profileModule,
                walletModule,
                pointsModule
            )
        }
    }
}

