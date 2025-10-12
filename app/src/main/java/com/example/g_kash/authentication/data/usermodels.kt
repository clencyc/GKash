package com.example.g_kash.authentication.data

import kotlinx.serialization.Serializable

// Original registration models (keeping for backward compatibility)
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

// KYC Models for new ID + Selfie registration flow
@Serializable
data class KycIdUploadResponse(
    val success: Boolean,
    val message: String,
    val temp_token: String,
    val user_id: String,
    val verified: Boolean,
    val score: Int,
    val extractedData: ExtractedIdData
)

@Serializable
data class ExtractedIdData(
    val user_name: String,
    val user_nationalId: String,
    val dateOfBirth: String?
)

@Serializable
data class AddPhoneRequest(
    val phoneNumber: String
)

@Serializable
data class AddPhoneResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class UserData(
    val user_nationalId: String,
    val user_name: String,
    val phoneNumber: String
)

@Serializable
data class KycUserData(
    val user_nationalId: String,
    val user_name: String,
    val phoneNumber: String,
    val idVerified: Boolean
)

@Serializable
data class CreatePinRequest(
    val user_pin: String
)

@Serializable
data class CreatePinResponse(
    val success: Boolean,
    val message: String,
    val token: String,
    val user: KycUserData
)

@Serializable
data class LoginRequest(
    val user_nationalId: String, // Changed from phoneNumber to nationalId
    val user_pin: String // Changed from pin to user_pin
)

@Serializable
data class LoginResponse(
    val success: Boolean = true,
    val message: String,
    val token: String,
    val user: KycUserData
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