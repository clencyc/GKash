package com.example.g_kash.authentication.util

import android.util.Log
import com.example.g_kash.data.SessionStorage
import kotlinx.coroutines.flow.first

/**
 * Utility class for debugging authentication and token storage
 */
object AuthDebugUtils {
    
    suspend fun logCurrentAuthState(sessionStorage: SessionStorage, tag: String = "AuthDebug") {
        try {
            val token = sessionStorage.authTokenStream.first()
            val userId = sessionStorage.userIdStream.first()
            
            Log.d(tag, "=== AUTH STATE DEBUG ===")
            Log.d(tag, "Token: ${if (token != null) "Present (${token.take(10)}...)" else "NULL"}")
            Log.d(tag, "User ID: ${userId ?: "NULL"}")
            Log.d(tag, "========================")
        } catch (e: Exception) {
            Log.e(tag, "Error reading auth state", e)
        }
    }
    
    suspend fun verifyTokenPersistence(
        sessionStorage: SessionStorage, 
        expectedUserId: String? = null,
        tag: String = "AuthDebug"
    ): Boolean {
        return try {
            val token = sessionStorage.authTokenStream.first()
            val userId = sessionStorage.userIdStream.first()
            
            val hasToken = token != null
            val userIdMatches = expectedUserId == null || userId == expectedUserId
            
            Log.d(tag, "Token persistence check: hasToken=$hasToken, userIdMatches=$userIdMatches")
            
            hasToken && userIdMatches
        } catch (e: Exception) {
            Log.e(tag, "Error verifying token persistence", e)
            false
        }
    }
}