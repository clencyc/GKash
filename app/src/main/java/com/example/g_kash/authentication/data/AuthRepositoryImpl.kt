package com.example.g_kash.authentication.data

import com.example.g_kash.authentication.domain.AuthRepository
import com.example.g_kash.data.SessionStorage
import kotlinx.coroutines.flow.Flow
import java.io.IOException

class AuthRepositoryImpl(
    private val apiService: ApiService,
    private val sessionStorage: SessionStorage
) : AuthRepository {

    override fun getAuthTokenStream(): Flow<String?> = sessionStorage.authTokenStream

    // Registration and Authentication
    override suspend fun registerUser(
        name: String,
        email: String,
        pin: String,
        confirmPin: String
    ): Result<RegisterUserResponse> {
        android.util.Log.d("AUTH_REPO", "============================================")
        android.util.Log.d("AUTH_REPO", "registerUser() called")
        android.util.Log.d("AUTH_REPO", "name: $name")
        android.util.Log.d("AUTH_REPO", "email: $email")
        android.util.Log.d("AUTH_REPO", "pin length: ${pin.length}")
        android.util.Log.d("AUTH_REPO", "confirmPin length: ${confirmPin.length}")
        android.util.Log.d("AUTH_REPO", "============================================")
        
        return try {
            val request = RegisterUserRequest(name, email, pin, confirmPin)
            android.util.Log.d("AUTH_REPO", "Request object created: user_name=${request.user_name}, email=${request.email}")
            android.util.Log.d("AUTH_REPO", "Calling apiService.registerUser()...")
            
            val response = apiService.registerUser(request)
            
            android.util.Log.d("AUTH_REPO", "✓ API call completed successfully")
            android.util.Log.d("AUTH_REPO", "Response success: ${response.success}")
            android.util.Log.d("AUTH_REPO", "Response message: ${response.message}")
            android.util.Log.d("AUTH_REPO", "Response token: ${if (response.token != null) "Present (${response.token.take(20)}...)" else "NULL"}")
            android.util.Log.d("AUTH_REPO", "Response user: ${response.user}")
            
            // DON'T save session here - we're in the middle of KYC flow
            // The token will be saved after phone verification is complete
            // KycViewModel will handle saving the session after all KYC steps are done
            Result.success(response)
        } catch (e: Exception) {
            android.util.Log.e("AUTH_REPO", "✗ Exception in registerUser", e)
            android.util.Log.e("AUTH_REPO", "Exception type: ${e::class.simpleName}")
            android.util.Log.e("AUTH_REPO", "Exception message: ${e.message}")
            android.util.Log.e("AUTH_REPO", "Stack trace:", e)
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, pin: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(email, pin)
            val response = apiService.login(request)
            if (response.success) {
                sessionStorage.saveSession(
                    token = response.token,
                    userId = response.user?.id ?: "",
                    userName = response.user?.user_name,
                    userEmail = email
                )
            }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // KYC Related Methods
    override suspend fun registerWithId(
        idImageBytes: ByteArray,
        selfieBytes: ByteArray
    ): KycIdUploadResponse {
        return try {
            val response = apiService.registerWithId(idImageBytes, selfieBytes)
            if (response.success) {
                // Save the temp token for subsequent KYC steps
                sessionStorage.saveTempToken(response.temp_token)
            }
            response
        } catch (e: Exception) {
            throw when (e) {
                is IOException -> IOException("Network error. Please check your connection.", e)
                else -> Exception("Failed to process ID verification: ${e.message}", e)
            }
        }
    }

    override suspend fun addPhone(
        phoneNumber: String,
        tempToken: String
    ): AddPhoneResponse {
        return try {
            val request = AddPhoneRequest(phoneNumber)
            val response = apiService.addPhone(request, tempToken)
            if (response.success) {
                // Update session with phone number if needed
                sessionStorage.savePhoneNumber(phoneNumber)
            }
            response
        } catch (e: Exception) {
            throw when (e) {
                is IOException -> IOException("Network error. Please check your connection.", e)
                else -> Exception("Failed to add phone number: ${e.message}", e)
            }
        }
    }

    override suspend fun createPinKyc(
        pin: String,
        tempToken: String
    ): CreatePinResponse {
        return try {
            val request = CreatePinRequest(pin)
            val response = apiService.createPinKyc(request, tempToken)
            if (response.success) {
                // Clear temp token and update session with new auth token
                sessionStorage.clearTempToken()
                response.token?.let { token ->
                    sessionStorage.saveSession(
                        token = token,
                        userId = sessionStorage.getCurrentUserId() ?: "",
                        userName = sessionStorage.getCurrentUserName(),
                        userEmail = sessionStorage.getCurrentUserEmail()
                    )
                }
            }
            response
        } catch (e: Exception) {
            throw when (e) {
                is IOException -> IOException("Network error. Please check your connection.", e)
                else -> Exception("Failed to create PIN: ${e.message}", e)
            }
        }
    }

    override suspend fun createPin(pin: String): Result<CreatePinResponse> {
        return try {
            val request = CreatePinRequest(pin)
            val response = apiService.createPin(request)
            if (response.success && response.token != null && response.user != null) {
                sessionStorage.saveSession(
                    token = response.token,
                    userId = response.user.id ?: response.user.user_nationalId,
                    userName = response.user.user_name,
                    userEmail = sessionStorage.getCurrentUserEmail()
                )
            }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {
            // Clear all session data
            sessionStorage.clearSession()
        } catch (e: Exception) {
            // Log the error but don't throw, as we want to ensure logout completes
            e.printStackTrace()
        }
    }

    override suspend fun saveSessionAfterKyc(
        token: String,
        userId: String,
        userName: String,
        userEmail: String
    ) {
        sessionStorage.saveSession(
            token = token,
            userId = userId,
            userName = userName,
            userEmail = userEmail
        )
    }
}
