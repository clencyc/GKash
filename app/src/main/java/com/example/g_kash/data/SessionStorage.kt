package com.example.g_kash.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Create a DataStore instance at the top level of your file
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

class SessionStorage(private val context: Context) {

    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
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

    suspend fun saveSession(token: String, userId: String) {
        android.util.Log.d("SessionStorage", "Saving session - Token: ${token.substring(0, minOf(10, token.length))}..., UserId: $userId")
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
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
        }
        android.util.Log.d("SessionStorage", "Session cleared")
    }
    
    // Debug function to log current session state
    suspend fun logCurrentSession(tag: String = "SessionStorage") {
        try {
            val token = authTokenStream.first()
            val userId = userIdStream.first()
            android.util.Log.d(tag, "=== SESSION STATE ===")
            android.util.Log.d(tag, "Token: ${if (token != null) "Present (${token.substring(0, minOf(10, token.length))}...)" else "NULL"}")
            android.util.Log.d(tag, "User ID: ${userId ?: "NULL"}")
            android.util.Log.d(tag, "====================")
        } catch (e: Exception) {
            android.util.Log.e(tag, "Error reading session state", e)
        }
    }
}