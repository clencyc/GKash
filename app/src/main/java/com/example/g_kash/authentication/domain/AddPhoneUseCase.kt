package com.example.g_kash.authentication.domain

import com.example.g_kash.authentication.data.AddPhoneResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddPhoneUseCase(
    private val authRepository: AuthRepository
) {
    data class Params(
        val phoneNumber: String,
        val tempToken: String
    )

    suspend operator fun invoke(params: Params): Result<AddPhoneResponse> {
        return withContext(Dispatchers.IO) {
            runCatching { authRepository.addPhone(params.phoneNumber, params.tempToken) }
        }
    }
}
