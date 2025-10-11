package com.example.g_kash.authentication.domain

import com.example.g_kash.authentication.data.AuthState
import com.example.g_kash.authentication.data.RegisterUserResponse
import com.example.g_kash.authentication.data.CreatePinResponse
import com.example.g_kash.authentication.data.LoginResponse
import kotlinx.coroutines.flow.Flow

// Repository interface
interface AuthRepository {
    fun getAuthTokenStream(): Flow<String?>

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