package com.example.g_kash.otp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.data.SessionStorage
import com.example.g_kash.otp.domain.SendOtpUseCase
import com.example.g_kash.otp.domain.VerifyOtpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OtpViewModel(
    private val sessionStorage: SessionStorage,
    private val sendOtpUseCase: SendOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OtpUiState())
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    fun sendOtp(phoneNumber: String, userName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val sendResult: Result<com.example.g_kash.otp.data.SendOtpResponse> = try {
                sendOtpUseCase(
                    phoneNumber = phoneNumber,
                    userName = userName
                )
            } catch (e: Exception) {
                Result.failure(e)
            }

            sendResult.fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.value = _uiState.value.copy(
                            isOtpSent = true,
                            isLoading = false,
                            error = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = response.message,
                            isLoading = false,
                            isOtpSent = false
                        )
                    }
                },
                onFailure = { error: Throwable ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to send OTP",
                        isLoading = false,
                        isOtpSent = false
                    )
                }
            )
        }
    }

    fun verifyOtp(otp: String, phoneNumber: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val verifyResult: Result<com.example.g_kash.otp.data.VerifyOtpResponse> = try {
                verifyOtpUseCase(
                    phoneNumber = phoneNumber,
                    otp = otp
                )
            } catch (e: Exception) {
                Result.failure(e)
            }

            verifyResult.fold(
                onSuccess = { response ->
                    if (response.success) {
                        _uiState.value = _uiState.value.copy(
                            isOtpVerified = true,
                            isLoading = false,
                            error = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = response.message,
                            isLoading = false,
                            isOtpVerified = false
                        )
                    }
                },
                onFailure = { error: Throwable ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to verify OTP",
                        isLoading = false,
                        isOtpVerified = false
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class OtpUiState(
    val isOtpSent: Boolean = false,
    val isOtpVerified: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
