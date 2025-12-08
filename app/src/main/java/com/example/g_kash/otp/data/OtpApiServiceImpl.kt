package com.example.g_kash.otp.data

import android.util.Log
import com.example.g_kash.otp.domain.OtpApiService
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
        private const val SEND_OTP_ENDPOINT = "$BASE_URL/api/otp/send"
        private const val VERIFY_OTP_ENDPOINT = "$BASE_URL/api/otp/verify"
    }

    override suspend fun sendOtp(phone: String, name: String): Result<SendOtpResponse> {
        return try {
            Log.d(TAG, "⏳ Sending OTP request...")
            
            // Ensure phone number includes country code
            val formattedPhone = if (phone.startsWith("+")) phone else "+254${phone.removePrefix("0")}"
            Log.d(TAG, "Formatted phone: $formattedPhone")
            
            val jsonBody = """{"phone":"$formattedPhone","name":"$name"}"""
            
            var responseText = ""
            var statusCodeValue = 0
            try {
                val response = httpClient.post(SEND_OTP_ENDPOINT) {
                    contentType(ContentType.Application.Json)
                    setBody(jsonBody)
                    timeout {
                        requestTimeoutMillis = 60000 // 60 seconds for OTP send
                    }
                }
                responseText = response.body()
                statusCodeValue = response.status.value
            } catch (e: ClientRequestException) { // 4xx
                responseText = e.response.body()
                statusCodeValue = e.response.status.value
                Log.w(TAG, "Send OTP got 4xx: ${e.response.status}")
            } catch (e: ServerResponseException) { // 5xx
                responseText = e.response.body()
                statusCodeValue = e.response.status.value
                Log.w(TAG, "Send OTP got 5xx: ${e.response.status}")
            }
            
            // Log raw response for debugging
            Log.d(TAG, "Raw OTP API response: $responseText")
            Log.d(TAG, "Response status code: $statusCodeValue")
            
            // Parse the response JSON to check the success field
            val (isSuccess, failureMessage) = try {
                val jsonElement = Json.parseToJsonElement(responseText)
                val jsonObj = jsonElement.jsonObject
                val success = jsonObj["success"]?.jsonPrimitive?.content?.toBoolean() ?: false
                val message = jsonObj["message"]?.jsonPrimitive?.content ?: "Failed to send OTP"
                
                Log.d(TAG, "Parsed response - success: $success, message: $message")
                
                success to message
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse response JSON, checking text: ${e.message}")
                // Fallback: check if response contains success indicators
                val ok = responseText.contains("\"success\":true")
                ok to "Failed to send OTP"
            }
            
            val otpResponse = SendOtpResponse(
                success = isSuccess,
                message = if (isSuccess) "OTP sent successfully" else failureMessage
            )
            
            if (otpResponse.success) {
                Log.d(TAG, "✓ OTP sent successfully to $formattedPhone")
            } else {
                Log.w(TAG, "✗ Failed to send OTP. Response: $responseText")
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
            
            // Manually create JSON with "phone" field for verify endpoint (matches backend)
            val jsonBody = """{"phone":"$formattedPhone","otp":"$otp"}"""
            Log.d(TAG, "Sending OTP verify request body: $jsonBody")
            
            var responseText = ""
            var statusCodeValue = 0
            try {
                val response = httpClient.post(VERIFY_OTP_ENDPOINT) {
                    contentType(ContentType.Application.Json)
                    setBody(jsonBody)
                    timeout {
                        requestTimeoutMillis = 20000 // 20 seconds for OTP verify (faster since server should be awake)
                    }
                }
                responseText = response.body()
                statusCodeValue = response.status.value
            } catch (e: ClientRequestException) {
                responseText = e.response.body()
                statusCodeValue = e.response.status.value
                Log.w(TAG, "Verify OTP got 4xx: ${e.response.status}")
            } catch (e: ServerResponseException) {
                responseText = e.response.body()
                statusCodeValue = e.response.status.value
                Log.w(TAG, "Verify OTP got 5xx: ${e.response.status}")
            }
            
            Log.d(TAG, "Raw OTP verify response: $responseText")
            Log.d(TAG, "Verify response status: $statusCodeValue")
            
            val responseJson = try { Json.parseToJsonElement(responseText).jsonObject } catch (_: Exception) { null }
            val backendMessage = responseJson?.get("message")?.jsonPrimitive?.content
            val backendValid = responseJson?.get("valid")?.jsonPrimitive?.content?.toBooleanStrictOrNull()

            val isSuccess = (statusCodeValue in 200..299) && (backendValid != false)
            val verifyResponse = VerifyOtpResponse(
                success = isSuccess,
                message = when {
                    isSuccess -> "OTP verified successfully"
                    backendMessage != null -> backendMessage
                    else -> "OTP verification failed"
                }
            )
            
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