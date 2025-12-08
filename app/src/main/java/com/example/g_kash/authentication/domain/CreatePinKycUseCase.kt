package com.example.g_kash.authentication.domain

import com.example.g_kash.authentication.data.CreatePinResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreatePinKycUseCase(
    private val authRepository: AuthRepository
) {
    data class Params(
        val pin: String,
        val tempToken: String
    )

    suspend operator fun invoke(params: Params): Result<CreatePinResponse> {
        return withContext(Dispatchers.IO) {
            runCatching { authRepository.createPinKyc(params.pin, params.tempToken) }
        }
    }
}
