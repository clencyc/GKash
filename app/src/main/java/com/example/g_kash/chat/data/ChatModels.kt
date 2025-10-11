package com.example.g_kash.chat.data

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val message: String,
    val sessionId: String
)

@Serializable
data class ChatResponse(
    val response: String,
    val sessionId: String,
    val timestamp: String
)

@Serializable
data class ResetRequest(
    val sessionId: String
)

@Serializable
data class ResetResponse(
    val message: String,
    val sessionId: String,
    val timestamp: String
)

@Serializable
data class DeleteSessionRequest(
    val sessionId: String
)

@Serializable
data class DeleteSessionResponse(
    val message: String,
    val sessionId: String
)

@Serializable
data class HealthCheckResponse(
    val status: String,
    val service: String,
    val timestamp: String
)

@Serializable
data class ErrorResponse(
    val error: String
)