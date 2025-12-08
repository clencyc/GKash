package com.example.g_kash.authentication.domain

import com.example.g_kash.authentication.data.AuthState
import com.example.g_kash.authentication.data.RegisterUserResponse
import com.example.g_kash.authentication.data.CreatePinResponse
import com.example.g_kash.authentication.data.LoginResponse
import com.example.g_kash.authentication.data.KycIdUploadResponse
import com.example.g_kash.authentication.data.AddPhoneResponse
import kotlinx.coroutines.flow.Flow

// Repository interface
interface AuthRepository {
    fun getAuthTokenStream(): Flow<String?>

    // Registration and Authentication
    suspend fun registerUser(
        name: String,
        email: String,
        pin: String,
        confirmPin: String
    ): Result<RegisterUserResponse>

    suspend fun login(
        email: String,
        pin: String
    ): Result<LoginResponse>

    // KYC Related Methods
    suspend fun registerWithId(
        idImageBytes: ByteArray,
        selfieBytes: ByteArray
    ): KycIdUploadResponse

    suspend fun addPhone(
        phoneNumber: String,
        tempToken: String
    ): AddPhoneResponse

    suspend fun createPinKyc(
        pin: String,
        tempToken: String
    ): CreatePinResponse

    suspend fun createPin(pin: String): Result<CreatePinResponse>

    suspend fun saveSessionAfterKyc(
        token: String,
        userId: String,
        userName: String,
        userEmail: String
    )

    suspend fun logout()
}

// Use cases
class CreateAccountUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        name: String,
        email: String,
        pin: String,
        confirmPin: String
    ): Result<RegisterUserResponse> {
        android.util.Log.d("CREATE_ACCOUNT_USE_CASE", "============================================")
        android.util.Log.d("CREATE_ACCOUNT_USE_CASE", "INVOKED with parameters:")
        android.util.Log.d("CREATE_ACCOUNT_USE_CASE", "name: $name")
        android.util.Log.d("CREATE_ACCOUNT_USE_CASE", "email: $email")
        android.util.Log.d("CREATE_ACCOUNT_USE_CASE", "pin length: ${pin.length}")
        android.util.Log.d("CREATE_ACCOUNT_USE_CASE", "confirmPin length: ${confirmPin.length}")
        android.util.Log.d("CREATE_ACCOUNT_USE_CASE", "============================================")
        
        if (name.isBlank()) {
            android.util.Log.e("CREATE_ACCOUNT_USE_CASE", "✗ Validation failed: Name is blank")
            return Result.failure(Exception("Name is required"))
        }
        if (email.isBlank()) {
            android.util.Log.e("CREATE_ACCOUNT_USE_CASE", "✗ Validation failed: Email is blank")
            return Result.failure(Exception("Email is required"))
        }
        if (!email.contains("@")) {
            android.util.Log.e("CREATE_ACCOUNT_USE_CASE", "✗ Validation failed: Invalid email format")
            return Result.failure(Exception("Please enter a valid email"))
        }
        if (pin.length != 4 || !pin.all { it.isDigit() }) {
            android.util.Log.e("CREATE_ACCOUNT_USE_CASE", "✗ Validation failed: PIN invalid (length=${pin.length})")
            return Result.failure(Exception("PIN must be 4 digits"))
        }
        if (confirmPin.length != 4 || !confirmPin.all { it.isDigit() }) {
            android.util.Log.e("CREATE_ACCOUNT_USE_CASE", "✗ Validation failed: Confirm PIN invalid (length=${confirmPin.length})")
            return Result.failure(Exception("Confirm PIN must be 4 digits"))
        }
        if (pin != confirmPin) {
            android.util.Log.e("CREATE_ACCOUNT_USE_CASE", "✗ Validation failed: PINs do not match")
            return Result.failure(Exception("PINs do not match"))
        }

        android.util.Log.d("CREATE_ACCOUNT_USE_CASE", "✓ All validations passed")
        android.util.Log.d("CREATE_ACCOUNT_USE_CASE", "Calling repository.registerUser()...")
        
        try {
            val result = repository.registerUser(name, email, pin, confirmPin)
            android.util.Log.d("CREATE_ACCOUNT_USE_CASE", "Repository call completed")
            android.util.Log.d("CREATE_ACCOUNT_USE_CASE", "Result success: ${result.isSuccess}")
            android.util.Log.d("CREATE_ACCOUNT_USE_CASE", "Result failure: ${result.isFailure}")
            return result
        } catch (e: Exception) {
            android.util.Log.e("CREATE_ACCOUNT_USE_CASE", "✗ Exception in repository call", e)
            return Result.failure(e)
        }
    }
}

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        pin: String
    ): Result<LoginResponse> {
        if (email.isBlank()) return Result.failure(Exception("Email is required"))
        if (pin.length != 4) return Result.failure(Exception("PIN must be 4 digits"))

        return repository.login(email, pin)
    }
}
