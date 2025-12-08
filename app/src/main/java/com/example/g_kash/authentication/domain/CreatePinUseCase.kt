package com.example.g_kash.authentication.domain

import com.example.g_kash.authentication.data.CreatePinResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreatePinUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(pin: String): Result<CreatePinResponse> {
        return withContext(Dispatchers.IO) { authRepository.createPin(pin) }
    }
}
