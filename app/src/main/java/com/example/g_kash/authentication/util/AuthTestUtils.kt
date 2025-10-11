package com.example.g_kash.authentication.util

import android.util.Log
import com.example.g_kash.data.SessionStorage
import kotlinx.coroutines.flow.first

/**
 * Utility for testing authentication flow and token storage
 */
object AuthTestUtils {
    
    /**
     * Test token storage by saving and immediately reading back
     */
    suspend fun testTokenStorage(sessionStorage: SessionStorage) {
        Log.d("AuthTest", "=== TESTING TOKEN STORAGE ===")
        
        try {
            // Test 1: Save a test token
            val testToken = "test_token_123456"
            val testUserId = "test_user_789"
            
            Log.d("AuthTest", "Saving test token...")
            sessionStorage.saveSession(testToken, testUserId)
            
            // Test 2: Read it back immediately
            val savedToken = sessionStorage.authTokenStream.first()
            val savedUserId = sessionStorage.userIdStream.first()
            
            Log.d("AuthTest", "Saved token: $savedToken")
            Log.d("AuthTest", "Saved user ID: $savedUserId")
            
            // Test 3: Verify they match
            val tokenMatches = savedToken == testToken
            val userIdMatches = savedUserId == testUserId
            
            Log.d("AuthTest", "Token matches: $tokenMatches")
            Log.d("AuthTest", "User ID matches: $userIdMatches")
            
            if (tokenMatches && userIdMatches) {
                Log.d("AuthTest", "✅ Token storage test PASSED")
            } else {
                Log.e("AuthTest", "❌ Token storage test FAILED")
            }
            
        } catch (e: Exception) {
            Log.e("AuthTest", "❌ Token storage test ERROR", e)
        }
        
        Log.d("AuthTest", "============================")
    }
    
    /**
     * Test clearing session
     */
    suspend fun testSessionClear(sessionStorage: SessionStorage) {
        Log.d("AuthTest", "=== TESTING SESSION CLEAR ===")
        
        try {
            // First save something
            sessionStorage.saveSession("temp_token", "temp_user")
            
            // Then clear it
            sessionStorage.clearSession()
            
            // Check if it's actually cleared
            val token = sessionStorage.authTokenStream.first()
            val userId = sessionStorage.userIdStream.first()
            
            val isCleared = token == null && userId == null
            
            Log.d("AuthTest", "Token after clear: $token")
            Log.d("AuthTest", "User ID after clear: $userId")
            Log.d("AuthTest", "Session cleared: $isCleared")
            
            if (isCleared) {
                Log.d("AuthTest", "✅ Session clear test PASSED")
            } else {
                Log.e("AuthTest", "❌ Session clear test FAILED")
            }
            
        } catch (e: Exception) {
            Log.e("AuthTest", "❌ Session clear test ERROR", e)
        }
        
        Log.d("AuthTest", "==============================")
    }
}