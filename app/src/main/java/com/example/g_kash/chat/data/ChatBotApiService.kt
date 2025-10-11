package com.example.g_kash.chat.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * API Service for chatbot operations
 */
class ChatBotApiService(
    private val client: HttpClient,
    private val baseUrl: String = "https://gkash.onrender.com/api"
) {
    
    /**
     * Send a message to the chatbot
     * POST /chatbot/chat
     */
    suspend fun sendMessage(request: ChatRequest): Result<ChatResponse> {
        return try {
            val response = client.post("$baseUrl/chatbot/chat") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status == HttpStatusCode.OK) {
                val chatResponse: ChatResponse = response.body()
                Result.success(chatResponse)
            } else {
                val errorResponse: ErrorResponse = response.body()
                Result.failure(Exception("Chat failed: ${errorResponse.error}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * Reset chatbot conversation for a session
     * POST /chatbot/reset
     */
    suspend fun resetConversation(request: ResetRequest): Result<ResetResponse> {
        return try {
            val response = client.post("$baseUrl/chatbot/reset") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status == HttpStatusCode.OK) {
                val resetResponse: ResetResponse = response.body()
                Result.success(resetResponse)
            } else {
                Result.failure(Exception("Failed to reset conversation: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * Delete a specific chat session
     * DELETE /chatbot/session
     */
    suspend fun deleteSession(request: DeleteSessionRequest): Result<DeleteSessionResponse> {
        return try {
            val response = client.delete("$baseUrl/chatbot/session") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status == HttpStatusCode.OK) {
                val deleteResponse: DeleteSessionResponse = response.body()
                Result.success(deleteResponse)
            } else {
                Result.failure(Exception("Failed to delete session: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * Health check for chatbot service
     * GET /chatbot/health
     */
    suspend fun healthCheck(): Result<HealthCheckResponse> {
        return try {
            val response = client.get("$baseUrl/chatbot/health") {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val healthResponse: HealthCheckResponse = response.body()
                Result.success(healthResponse)
            } else {
                Result.failure(Exception("Health check failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}