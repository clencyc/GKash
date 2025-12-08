package com.example.g_kash.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsHelper {
    // Screen View Event
    fun logScreenView(screenName: String, screenClass: String, firebaseAnalytics: FirebaseAnalytics) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    // Custom Event
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap(), firebaseAnalytics: FirebaseAnalytics) {
        val bundle = Bundle()
        params.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Float -> bundle.putFloat(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                else -> bundle.putString(key, value.toString())
            }
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    // User Property
    fun setUserProperty(property: String, value: String, firebaseAnalytics: FirebaseAnalytics) {
        firebaseAnalytics.setUserProperty(property, value)
    }
}
