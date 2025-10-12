package com.example.g_kash.otp.domain

import com.example.g_kash.otp.data.SendOtpResponse
import com.example.g_kash.otp.data.VerifyOtpResponse

/**
 * Interface for OTP API operations
 */
interface OtpApiService {
    /**
     * Send OTP to the specified phone number
     * @param phone The phone number to send OTP to (should include country code)
     * @param name The user's name for personalization
     * @return SendOtpResponse indicating success/failure
     */
    suspend fun sendOtp(phone: String, name: String): Result<SendOtpResponse>
    
    /**
     * Verify the OTP code for the specified phone number
     * @param phone The phone number that received the OTP
     * @param otp The OTP code to verify
     * @return VerifyOtpResponse indicating success/failure
     */
    suspend fun verifyOtp(phone: String, otp: String): Result<VerifyOtpResponse>
}