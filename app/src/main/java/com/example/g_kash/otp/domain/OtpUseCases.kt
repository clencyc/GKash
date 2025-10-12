package com.example.g_kash.otp.domain

import com.example.g_kash.otp.data.SendOtpResponse
import com.example.g_kash.otp.data.VerifyOtpResponse

/**
 * Use case for sending OTP
 */
class SendOtpUseCase(private val otpApiService: OtpApiService) {
    suspend operator fun invoke(phoneNumber: String, userName: String): Result<SendOtpResponse> {
        // Validate input
        if (phoneNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("Phone number is required"))
        }
        
        if (userName.isBlank()) {
            return Result.failure(IllegalArgumentException("User name is required"))
        }
        
        // Clean phone number (remove spaces, hyphens, etc.)
        val cleanedPhone = phoneNumber.replace("\\s|-".toRegex(), "")
        
        // Validate phone number format (basic validation for Kenyan numbers)
        if (!isValidKenyanPhoneNumber(cleanedPhone)) {
            return Result.failure(IllegalArgumentException("Invalid phone number format"))
        }
        
        return otpApiService.sendOtp(cleanedPhone, userName)
    }
    
    private fun isValidKenyanPhoneNumber(phone: String): Boolean {
        // Accept formats:
        // +254xxxxxxxxx (13 digits total)
        // 254xxxxxxxxx (12 digits total)  
        // 0xxxxxxxxx (10 digits starting with 0)
        // xxxxxxxxx (9 digits)
        
        return when {
            phone.startsWith("+254") && phone.length == 13 -> true
            phone.startsWith("254") && phone.length == 12 -> true
            phone.startsWith("0") && phone.length == 10 -> true
            phone.length == 9 && phone.all { it.isDigit() } -> true
            else -> false
        }
    }
}

/**
 * Use case for verifying OTP
 */
class VerifyOtpUseCase(private val otpApiService: OtpApiService) {
    suspend operator fun invoke(phoneNumber: String, otp: String): Result<VerifyOtpResponse> {
        // Validate input
        if (phoneNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("Phone number is required"))
        }
        
        if (otp.isBlank()) {
            return Result.failure(IllegalArgumentException("OTP is required"))
        }
        
        // Validate OTP format (should be 6 digits)
        if (otp.length != 6 || !otp.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("OTP must be 6 digits"))
        }
        
        // Clean phone number (remove spaces, hyphens, etc.)
        val cleanedPhone = phoneNumber.replace("\\s|-".toRegex(), "")
        
        return otpApiService.verifyOtp(cleanedPhone, otp)
    }
}