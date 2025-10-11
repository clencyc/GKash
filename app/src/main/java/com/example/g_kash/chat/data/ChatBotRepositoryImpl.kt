package com.example.g_kash.chat.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.g_kash.chat.domain.ChatBotRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.*

private val Context.chatDataStore: DataStore<Preferences> by preferencesDataStore(name = "chat_prefs")

class ChatBotRepositoryImpl(
    private val apiService: ChatBotApiService,
    private val context: Context
) : ChatBotRepository {

    private val sessionIdKey = stringPreferencesKey("session_id")

    override suspend fun sendMessage(message: String): Result<ChatResponse> {
        val sessionId = getCurrentSessionId()
        val request = ChatRequest(message = message, sessionId = sessionId)
        return apiService.sendMessage(request)
    }

    override suspend fun resetConversation(): Result<ResetResponse> {
        val sessionId = getCurrentSessionId()
        val request = ResetRequest(sessionId = sessionId)
        val result = apiService.resetConversation(request)
        
        // If reset is successful, generate a new session
        if (result.isSuccess) {
            generateNewSession()
        }
        
        return result
    }

    override suspend fun deleteSession(): Result<DeleteSessionResponse> {
        val sessionId = getCurrentSessionId()
        val request = DeleteSessionRequest(sessionId = sessionId)
        val result = apiService.deleteSession(request)
        
        // If deletion is successful, generate a new session
        if (result.isSuccess) {
            generateNewSession()
        }
        
        return result
    }

    override suspend fun healthCheck(): Result<HealthCheckResponse> {
        return apiService.healthCheck()
    }

    override suspend fun getCurrentSessionId(): String {
        return context.chatDataStore.data.map { preferences ->
            preferences[sessionIdKey]
        }.first() ?: generateNewSession()
    }

    override suspend fun generateNewSession(): String {
        val newSessionId = "user-${UUID.randomUUID()}-session"
        
        context.chatDataStore.edit { preferences ->
            preferences[sessionIdKey] = newSessionId
        }
        
        return newSessionId
    }
}