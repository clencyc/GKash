package com.example.g_kash.authentication.data

import kotlinx.serialization.Serializable

@Serializable
data class RegisterUserRequest(
    val user_name: String,
    val phoneNumber: String,
    val user_nationalId: String
)

@Serializable
data class RegisterUserResponse(
    val message: String,
    val temp_token: String,
    val user_id: String
)

@Serializable
data class UserData(
    val user_nationalId: String,
    val user_name: String,
    val phoneNumber: String
)

@Serializable
data class CreatePinRequest(
    val user_pin: String
)

@Serializable
data class CreatePinResponse(
    val message: String,
    val token: String,
    val user: UserData
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