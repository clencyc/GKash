package com.example.g_kash.otp.data

import android.util.Log
import com.example.g_kash.otp.domain.OtpApiService
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException

/**
 * Implementation of OTP API service using Ktor HTTP client
 * Communicates with Tiara Connect OTP service
 */
class OtpApiServiceImpl(
    private val httpClient: HttpClient
) : OtpApiService {

    companion object {
        private const val TAG = "OtpApiService"
        private const val BASE_URL = "https://tiara-connect-otp.onrender.com"
        private const val SEND_OTP_ENDPOINT = "$BASE_URL/api/auth/send-otp"
        private const val VERIFY_OTP_ENDPOINT = "$BASE_URL/api/auth/verify-otp"
    }

    override suspend fun sendOtp(phone: String, name: String): Result<SendOtpResponse> {
        return try {
            Log.d(TAG, "Sending OTP to phone: $phone, name: $name")
            
            // Ensure phone number includes country code
            val formattedPhone = if (phone.startsWith("+")) phone else "+254${phone.removePrefix("0")}"
            Log.d(TAG, "Formatted phone number: $formattedPhone")
            
            val request = SendOtpRequest(
                phone = formattedPhone,
                name = name.ifBlank { "User" }
            )
            
            val response = httpClient.post(SEND_OTP_ENDPOINT) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            val otpResponse: SendOtpResponse = response.body()
            
            if (otpResponse.success) {
                Log.d(TAG, "OTP sent successfully to $formattedPhone")
            } else {
                Log.w(TAG, "Failed to send OTP: ${otpResponse.message}")
            }
            
            Result.success(otpResponse)
        } catch (e: CancellationException) {
            Log.w(TAG, "OTP send request was cancelled")
            throw e // Don't wrap cancellation exceptions
        } catch (e: Exception) {
            Log.e(TAG, "Error sending OTP to $phone", e)
            Result.failure(e)
        }
    }

    override suspend fun verifyOtp(phone: String, otp: String): Result<VerifyOtpResponse> {
        return try {
            Log.d(TAG, "Verifying OTP for phone: $phone, otp: $otp")
            
            // Ensure phone number includes country code
            val formattedPhone = if (phone.startsWith("+")) phone else "+254${phone.removePrefix("0")}"
            Log.d(TAG, "Formatted phone number: $formattedPhone")
            
            val request = VerifyOtpRequest(
                phone = formattedPhone,
                otp = otp
            )
            
            val response = httpClient.post(VERIFY_OTP_ENDPOINT) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            val verifyResponse: VerifyOtpResponse = response.body()
            
            if (verifyResponse.success) {
                Log.d(TAG, "OTP verified successfully for $formattedPhone")
            } else {
                Log.w(TAG, "OTP verification failed: ${verifyResponse.message}")
            }
            
            Result.success(verifyResponse)
        } catch (e: CancellationException) {
            Log.w(TAG, "OTP verify request was cancelled")
            throw e // Don't wrap cancellation exceptions
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying OTP for $phone", e)
            Result.failure(e)
        }
    }
}