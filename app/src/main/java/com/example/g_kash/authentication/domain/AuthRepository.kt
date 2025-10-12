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

    // Original methods (keeping for backward compatibility)
    suspend fun registerUser(
        name: String,
        phoneNumber: String,
        idNumber: String
    ): Result<RegisterUserResponse>

    suspend fun createPin(
        userId: String,
        pin: String
    ): Result<CreatePinResponse>

    suspend fun login(
        phoneNumber: String,
        pin: String
    ): Result<LoginResponse>

    // New KYC methods
    suspend fun registerWithId(
        idImageBytes: ByteArray,
        selfieBytes: ByteArray
    ): Result<KycIdUploadResponse>

    suspend fun addPhone(
        phoneNumber: String
    ): Result<AddPhoneResponse>

    suspend fun createPinKyc(
        pin: String
    ): Result<CreatePinResponse>

    suspend fun loginWithNationalId(
        nationalId: String,
        pin: String
    ): Result<LoginResponse>

    suspend fun logout()
}

// Use cases
class CreateAccountUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        name: String,
        phoneNumber: String,
        idNumber: String
    ): Result<RegisterUserResponse> {
        if (name.isBlank()) return Result.failure(Exception("Name is required"))
        if (phoneNumber.isBlank()) return Result.failure(Exception("Phone number is required"))
        if (idNumber.isBlank()) return Result.failure(Exception("ID number is required"))

        return repository.registerUser(name, phoneNumber, idNumber)
    }
}

class CreatePinUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        userId: String,
        pin: String
    ): Result<CreatePinResponse> {
        if (pin.length != 4) return Result.failure(Exception("PIN must be 4 digits"))
        if (!pin.all { it.isDigit() }) return Result.failure(Exception("PIN must contain only digits"))

        return repository.createPin(userId, pin)
    }
}

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        phoneNumber: String,
        pin: String
    ): Result<LoginResponse> {
        if (phoneNumber.isBlank()) return Result.failure(Exception("Phone number is required"))
        if (pin.length != 4) return Result.failure(Exception("PIN must be 4 digits"))

        return repository.login(phoneNumber, pin)
    }
}

// KYC Use Cases
class RegisterWithIdUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        idImageBytes: ByteArray,
        selfieBytes: ByteArray
    ): Result<KycIdUploadResponse> {
        if (idImageBytes.isEmpty()) return Result.failure(Exception("ID image is required"))
        if (selfieBytes.isEmpty()) return Result.failure(Exception("Selfie is required"))

        return repository.registerWithId(idImageBytes, selfieBytes)
    }
}

class AddPhoneUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        phoneNumber: String
    ): Result<AddPhoneResponse> {
        if (phoneNumber.isBlank()) return Result.failure(Exception("Phone number is required"))
        if (phoneNumber.length < 10) return Result.failure(Exception("Invalid phone number format"))

        return repository.addPhone(phoneNumber)
    }
}

class CreatePinKycUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        pin: String
    ): Result<CreatePinResponse> {
        if (pin.length != 4) return Result.failure(Exception("PIN must be 4 digits"))
        if (!pin.all { it.isDigit() }) return Result.failure(Exception("PIN must contain only digits"))

        return repository.createPinKyc(pin)
    }
}

class LoginWithNationalIdUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        nationalId: String,
        pin: String
    ): Result<LoginResponse> {
        if (nationalId.isBlank()) return Result.failure(Exception("National ID is required"))
        if (pin.length != 4) return Result.failure(Exception("PIN must be 4 digits"))

        return repository.loginWithNationalId(nationalId, pin)
    }
}
