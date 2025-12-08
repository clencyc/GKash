package com.example.g_kash.otp.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Request model for sending OTP
 */
@Serializable
data class SendOtpRequest(
    @SerialName("phone")
    val phoneNumber: String,
    val name: String
)

/**
 * Response model for sending OTP
 */
@Serializable
data class SendOtpResponse(
    val success: Boolean,
    val message: String
)

/**
 * Request model for verifying OTP
 */
@Serializable
data class VerifyOtpRequest(
    @SerialName("phone")
    val phoneNumber: String,
    val otp: String
)

/**
 * Response model for verifying OTP
 */
@Serializable
data class VerifyOtpResponse(
    val success: Boolean,
    val message: String
)