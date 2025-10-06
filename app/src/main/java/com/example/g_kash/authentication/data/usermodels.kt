package com.example.g_kash.authentication.data

import kotlinx.serialization.Serializable

@Serializable
data class CreateAccountRequest(
    val name: String,
    val phoneNumber: String,
    val idNumber: String
)

@Serializable
data class CreateAccountResponse(
    val success: Boolean,
    val message: String,
    val userId: String? = null,
    val data: UserData? = null
)

@Serializable
data class UserData(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val idNumber: String,
    val createdAt: String
)

@Serializable
data class CreatePinRequest(
    val userId: String,
    val pin: String
)

@Serializable
data class CreatePinResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class LoginRequest(
    val phoneNumber: String,
    val pin: String
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val user: UserData? = null
)

@Serializable
data class ApiError(
    val success: Boolean = false,
    val message: String,
    val error: String? = null
)

// Domain models
@Serializable
data class User(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val idNumber: String
)

@Serializable
data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val error: String? = null
)