package com.example.g_kash.authentication.domain

import com.example.g_kash.authentication.data.KycIdUploadResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegisterWithIdUseCase(
    private val authRepository: AuthRepository
) {
    data class Params(
        val idImageBytes: ByteArray,
        val selfieBytes: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Params
            if (!idImageBytes.contentEquals(other.idImageBytes)) return false
            if (!selfieBytes.contentEquals(other.selfieBytes)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = idImageBytes.contentHashCode()
            result = 31 * result + selfieBytes.contentHashCode()
            return result
        }
    }

    suspend operator fun invoke(params: Params): Result<KycIdUploadResponse> {
        return withContext(Dispatchers.IO) {
            runCatching { authRepository.registerWithId(params.idImageBytes, params.selfieBytes) }
        }
    }
}
