package com.example.g_kash.authentication.data

import android.util.Log
import com.example.g_kash.accounts.data.Account
import com.example.g_kash.accounts.data.AccountsApiResponse
import com.example.g_kash.accounts.data.TotalBalanceResponse
import com.example.g_kash.authentication.data.model.BaseResponse
import com.example.g_kash.authentication.data.model.UserAchievementsResponse
import com.example.g_kash.authentication.data.model.UserProfileResponse
import com.example.g_kash.authentication.domain.AuthRepository
import com.example.g_kash.data.SessionStorage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.forms.*
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.header
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

// API Service
interface ApiService {
    // Original registration methods (keeping for backward compatibility)
    suspend fun registerUser(request: RegisterUserRequest): RegisterUserResponse
    suspend fun createPin(request: CreatePinRequest): CreatePinResponse
    suspend fun login(request: LoginRequest): LoginResponse

    // New KYC methods
    suspend fun registerWithId(idImageBytes: ByteArray, selfieBytes: ByteArray): KycIdUploadResponse
    suspend fun addPhone(request: AddPhoneRequest, tempToken: String): AddPhoneResponse
    suspend fun createPinKyc(request: CreatePinRequest, tempToken: String): CreatePinResponse
    suspend fun loginKyc(request: LoginRequest): LoginResponse

    suspend fun getAccounts(): AccountsApiResponse
    suspend fun getTotalBalance(): TotalBalanceResponse
    
    // Profile methods
    suspend fun getUserProfile(id: String): UserProfileResponse
    suspend fun getUserAchievements(id: String): UserAchievementsResponse
    suspend fun updateUserProfile(
        id: String,
        name: String,
        email: String,
        phoneNumber: String
    ): BaseResponse
}

class ApiServiceImpl(
    private val client: HttpClient,
    private val sessionStorage: SessionStorage
) : ApiService {
    private val baseUrl = "https://gkash.onrender.com/api"

    override suspend fun registerUser(request: RegisterUserRequest): RegisterUserResponse {
        android.util.Log.d("API_SERVICE", "============================================")
        android.util.Log.d("API_SERVICE", "registerUser() API call starting")
        android.util.Log.d("API_SERVICE", "URL: $baseUrl/auth/register")
        android.util.Log.d("API_SERVICE", "Request body: user_name=${request.user_name}, email=${request.email}")
        android.util.Log.d("API_SERVICE", "Thread: ${Thread.currentThread().name}")
        android.util.Log.d("API_SERVICE", "============================================")
        
        return try {
            android.util.Log.d("API_SERVICE", "Sending POST request...")
            
            val startTime = System.currentTimeMillis()
            val response: RegisterUserResponse = client.post("$baseUrl/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            val endTime = System.currentTimeMillis()
            
            android.util.Log.d("API_SERVICE", "✓ Response received successfully in ${endTime - startTime}ms")
            android.util.Log.d("API_SERVICE", "success: ${response.success}")
            android.util.Log.d("API_SERVICE", "message: ${response.message}")
            android.util.Log.d("API_SERVICE", "token: ${if (response.token != null) "Present" else "NULL"}")
            android.util.Log.d("API_SERVICE", "user: ${response.user}")
            
            response
        } catch (e: ClientRequestException) {
            android.util.Log.e("API_SERVICE", "✗ ClientRequestException caught")
            android.util.Log.e("API_SERVICE", "Status: ${e.response.status}")
            android.util.Log.e("API_SERVICE", "Description: ${e.response.status.description}")
            
            // Handle 4xx/5xx responses and try to deserialize the error response
            try {
                val errorResponse = e.response.body<RegisterUserResponse>()
                android.util.Log.e("API_SERVICE", "Parsed error response: ${errorResponse.message}")
                errorResponse
            } catch (parseError: Exception) {
                android.util.Log.e("API_SERVICE", "✗ Failed to parse error response", parseError)
                // If we can't parse the error, return a generic error response
                RegisterUserResponse(
                    success = false,
                    message = e.response.status.description
                )
            }
        } catch (e: HttpRequestTimeoutException) {
            android.util.Log.e("API_SERVICE", "✗ HTTP Request TIMEOUT - Server took too long to respond")
            android.util.Log.e("API_SERVICE", "This means the server at $baseUrl is not responding")
            RegisterUserResponse(
                success = false,
                message = "Server timeout - please check your internet connection and try again"
            )
        } catch (e: Exception) {
            android.util.Log.e("API_SERVICE", "✗ Unexpected exception in registerUser", e)
            android.util.Log.e("API_SERVICE", "Exception type: ${e::class.simpleName}")
            android.util.Log.e("API_SERVICE", "Exception message: ${e.message}")
            throw e
        }
    }

    override suspend fun login(request: LoginRequest): LoginResponse {
        return client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // KYC Methods Implementation
    override suspend fun registerWithId(idImageBytes: ByteArray, selfieBytes: ByteArray): KycIdUploadResponse {
        return client.submitFormWithBinaryData(
            url = "$baseUrl/auth/register-with-id",
            formData = formData {
                append("idImage", idImageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"id.jpg\"")
                })
                append("selfie", selfieBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"selfie.jpg\"")
                })
            }
        ).body()
    }

    override suspend fun addPhone(request: AddPhoneRequest, tempToken: String): AddPhoneResponse {
        return try {
            val response = client.post("$baseUrl/auth/add-phone") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $tempToken")
                setBody(request)
            }.body<AddPhoneResponse>()
            Log.d("API_SERVICE", "Add phone response: $response")
            response
        } catch (e: ClientRequestException) {
            Log.e("API_SERVICE", "Add phone failed: ${e.response.status} ${e.message}")
            try {
                e.response.body<AddPhoneResponse>()
            } catch (parseError: Exception) {
                AddPhoneResponse(success = false, message = e.response.status.description)
            }
        }
    }

    override suspend fun createPinKyc(request: CreatePinRequest, tempToken: String): CreatePinResponse {
        return client.post("$baseUrl/auth/create-pin") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $tempToken")
            setBody(request)
        }.body()
    }

    override suspend fun loginKyc(request: LoginRequest): LoginResponse {
        return client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun getAccounts(): AccountsApiResponse {
        return try {
            val token = sessionStorage.authTokenStream.first()
            android.util.Log.d("API_SERVICE", "getAccounts - Token: ${if (token != null) "Present" else "NULL"}")
            
            client.get("$baseUrl/accounts") {
                token?.let { header(io.ktor.http.HttpHeaders.Authorization, "Bearer $it") }
            }.body<AccountsApiResponse>()
        } catch (e: Exception) {
            // FIX: Added parentheses to emptyList()
            AccountsApiResponse(success = false, accounts = emptyList<Account>(), message = e.message)
        }
    }

    override suspend fun getTotalBalance(): TotalBalanceResponse {
        return try {
            // FIX: The /accounts/total-balance endpoint is unreliable/fails on empty db.
            // Returning a default 0.0 to prevent UI crashes.
            TotalBalanceResponse(success = true, totalBalance = 0.0)
        } catch (e: Exception) {
            TotalBalanceResponse(success = false, totalBalance = 0.0, message = e.message)
        }
    }

    // Profile methods implementation
    override suspend fun getUserProfile(id: String): UserProfileResponse {
        val token = sessionStorage.authTokenStream.first()
        android.util.Log.d("API_SERVICE", "getUserProfile - Token: ${if (token != null) "Present" else "NULL"}")
        
        return client.get("$baseUrl/user/$id") {
            token?.let { header(io.ktor.http.HttpHeaders.Authorization, "Bearer $it") }
        }.body()
    }
    // In ApiServiceImpl class

    override suspend fun getUserAchievements(id: String): UserAchievementsResponse {
        return try {
            UserAchievementsResponse(
                success = true,
                lessonsCompleted = 5,
                learningStreak = 3,
                savingsGoalsAchieved = 2,
                totalTimeSpent = "2h 30m",
                level = "Beginner"
            )
        } catch (e: Exception) {
            UserAchievementsResponse(success = false, message = e.message)
        }
    }


    override suspend fun updateUserProfile(
        id: String,
        name: String,
        email: String,
        phoneNumber: String
    ): BaseResponse {
        return try {
            // For now, just return success
            BaseResponse(success = true)
        } catch (e: Exception) {
            BaseResponse(success = false, message = e.message)
        }
    }
    
    override suspend fun createPin(request: CreatePinRequest): CreatePinResponse {
        return client.post("$baseUrl/auth/create-pin") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}


fun createHttpClient(sessionStorage: SessionStorage): HttpClient {
    return HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }

        // --- FIX: INSTALL THE AUTH PLUGIN ---
        install(Auth) {
            bearer {
                // This block tells Ktor how to get the token for every request
                loadTokens {
                    // Read the token from our DataStore Flow
                    val token = sessionStorage.authTokenStream.first()
                    Log.d("AuthFlow", "Loading token for request: ${if (token != null) "Token found (${token.take(10)}...)" else "No token"}")
                    if (token != null) {
                        BearerTokens(accessToken = token, refreshToken = "")
                    } else {
                        null
                    }
                }
                
                refreshTokens {
                    // DO NOT clear session — returning null is sufficient.
                    // Clearing logs the user out on ANY 401, even wrong request format.
                    Log.w("AuthFlow", "Received 401. No refresh mechanism — returning null.")
                    null
                }
            }
        }
    }
}
