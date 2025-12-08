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

    // Common Events
    object Events {
        // Screen Views
        const val SCREEN_WELCOME = "screen_welcome"
        const val SCREEN_CREATE_ACCOUNT = "screen_create_account"
        const val SCREEN_ADD_PHONE = "screen_add_phone"
        const val SCREEN_VERIFY_PHONE = "screen_verify_phone"
        const val SCREEN_CONFIRM_PIN = "screen_confirm_pin"
        const val SCREEN_REGISTRATION_COMPLETE = "screen_registration_complete"
        
        // Registration Events
        const val ACCOUNT_CREATED = "account_created"
        const val PHONE_ADDED = "phone_added"
        const val PHONE_VERIFIED = "phone_verified"
        const val OTP_SENT = "otp_sent"
        const val OTP_VERIFIED = "otp_verified"
        const val PIN_CONFIRMED = "pin_confirmed"
        const val REGISTRATION_COMPLETED = "registration_completed"
        
        // User Actions
        const val BUTTON_CLICKED = "button_clicked"
        const val FIELD_FILLED = "field_filled"
        const val ERROR_OCCURRED = "error_occurred"
        const val RESEND_OTP = "resend_otp"
        const val BACK_NAVIGATION = "back_navigation"
    }

    object UserProperties {
        const val REGISTRATION_STAGE = "registration_stage"
        const val PHONE_VERIFIED = "phone_verified"
        const val ACCOUNT_STATUS = "account_status"
    }
}
