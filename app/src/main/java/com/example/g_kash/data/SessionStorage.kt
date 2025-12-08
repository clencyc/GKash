package com.example.g_kash.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Create a DataStore instance at the top level of your file
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

class SessionStorage(private val context: Context) {

    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val TEMP_TOKEN_KEY = stringPreferencesKey("temp_token")
        private val PHONE_NUMBER_KEY = stringPreferencesKey("phone_number")
    }

    // Flow to observe the auth token
    val authTokenStream: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[AUTH_TOKEN_KEY]
        }

    val userIdStream: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID_KEY]
        }
        
    val userNameStream: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_NAME_KEY]
        }
        
    val userEmailStream: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_EMAIL_KEY]
        }

    suspend fun saveSession(
        token: String, 
        userId: String, 
        userName: String? = null, 
        userEmail: String? = null
    ) {
        android.util.Log.d("SessionStorage", "Saving session - " +
            "Token: ${token.substring(0, minOf(10, token.length))}..., " +
            "UserId: $userId, " +
            "UserName: ${userName?.take(5)}..., " +
            "Email: ${userEmail?.take(5)}...")
            
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
            userName?.let { preferences[USER_NAME_KEY] = it }
            userEmail?.let { preferences[USER_EMAIL_KEY] = it }
        }
        android.util.Log.d("SessionStorage", "Session saved successfully")
    }

    // Function to save the auth token
    suspend fun saveAuthToken(token: String) {
        android.util.Log.d("SessionStorage", "Saving auth token: ${token.substring(0, minOf(10, token.length))}...")
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
        }
        android.util.Log.d("SessionStorage", "Auth token saved successfully")
    }

    // Function to clear the auth token (on logout)
    suspend fun clearAuthToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
        }
    }
    
    // Function to clear all session data (token and user ID)
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(TEMP_TOKEN_KEY)
            preferences.remove(PHONE_NUMBER_KEY)
        }
        android.util.Log.d("SessionStorage", "Session cleared")
    }
    
    // Debug function to log current session state
    suspend fun logCurrentSession(tag: String = "SessionStorage") {
        try {
            val token = authTokenStream.first()
            val userId = userIdStream.first()
            val userName = userNameStream.first()
            val userEmail = userEmailStream.first()
            
            android.util.Log.d(tag, "=== SESSION STATE ===")
            android.util.Log.d(tag, "Token: ${if (token != null) "Present (${token.take(10)}...)" else "NULL"}")
            android.util.Log.d(tag, "User ID: ${userId ?: "NULL"}")
            android.util.Log.d(tag, "User Name: ${userName ?: "NULL"}")
            android.util.Log.d(tag, "User Email: ${userEmail ?: "NULL"}")
            android.util.Log.d(tag, "====================")
        } catch (e: Exception) {
            android.util.Log.e(tag, "Error reading session state", e)
        }
    }
    
    // KYC Related Methods
    suspend fun saveTempToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TEMP_TOKEN_KEY] = token
        }
        Log.d("SessionStorage", "Temporary token saved")
    }

    suspend fun clearTempToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TEMP_TOKEN_KEY)
        }
        Log.d("SessionStorage", "Temporary token cleared")
    }

    suspend fun savePhoneNumber(phoneNumber: String) {
        context.dataStore.edit { preferences ->
            preferences[PHONE_NUMBER_KEY] = phoneNumber
        }
        Log.d("SessionStorage", "Phone number saved: ${phoneNumber.take(3)}...")
    }

    // Helper functions to get current values (non-Flow)
    suspend fun getCurrentUserId(): String? = userIdStream.first()
    suspend fun getCurrentUserName(): String? = userNameStream.first()
    suspend fun getCurrentUserEmail(): String? = userEmailStream.first()
    suspend fun getCurrentToken(): String? = authTokenStream.first()
    suspend fun getTempToken(): String? = context.dataStore.data.map { it[TEMP_TOKEN_KEY] }.first()
    suspend fun getPhoneNumber(): String? = context.dataStore.data.map { it[PHONE_NUMBER_KEY] }.first()
    
    /**
     * Wait for token to be available with retry mechanism.
     * Useful after saving session to ensure token has propagated through DataStore.
     * @param maxRetries Maximum number of retry attempts
     * @param delayMs Delay between retries in milliseconds
     * @return The token if available, null if all retries exhausted
     */
    suspend fun waitForToken(maxRetries: Int = 5, delayMs: Long = 100): String? {
        // Alternative approach: Wait for Flow to emit a non-null token with timeout
        try {
            // Use authTokenStream to wait for actual Flow emission of non-null token
            // This is more reliable than calling first() on the current value
            val token = try {
                authTokenStream.first { it != null }
            } catch (e: Exception) {
                Log.d("SessionStorage", "Flow wait failed after retries: ${e.message}")
                null
            }
            
            if (token != null) {
                Log.d("SessionStorage", "Token available from Flow")
                return token
            }
        } catch (e: Exception) {
            Log.e("SessionStorage", "Error waiting for token from Flow", e)
        }
        
        // Fallback: Retry with explicit delays
        for (attempt in 1..maxRetries) {
            val token = getCurrentToken()
            if (token != null) {
                Log.d("SessionStorage", "Token available on attempt $attempt")
                return token
            }
            if (attempt < maxRetries) {
                Log.d("SessionStorage", "Token not yet available, retrying... (attempt $attempt/$maxRetries)")
                delay(delayMs)
            }
        }
        Log.w("SessionStorage", "Token still not available after $maxRetries retries")
        return null
    }
}