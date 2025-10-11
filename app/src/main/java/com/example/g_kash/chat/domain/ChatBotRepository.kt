package com.example.g_kash.chat.domain

import com.example.g_kash.chat.data.ChatResponse
import com.example.g_kash.chat.data.ResetResponse
import com.example.g_kash.chat.data.DeleteSessionResponse
import com.example.g_kash.chat.data.HealthCheckResponse

interface ChatBotRepository {
    suspend fun sendMessage(message: String): Result<ChatResponse>
    suspend fun resetConversation(): Result<ResetResponse>
    suspend fun deleteSession(): Result<DeleteSessionResponse>
    suspend fun healthCheck(): Result<HealthCheckResponse>
    suspend fun getCurrentSessionId(): String
    suspend fun generateNewSession(): String
}