package com.example.g_kash.authentication.presentation

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.authentication.data.ExtractedIdData
import com.example.g_kash.authentication.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// KYC UI State
data class KycUiState(
    val currentStep: KycStep = KycStep.WELCOME,
    val isLoading: Boolean = false,
    val error: String? = null,
    val progress: Float = 0.0f,
    
    // ID Verification
    val idImageUri: Uri? = null,
    val selfieUri: Uri? = null,
    val extractedData: ExtractedIdData? = null,
    val verificationScore: Int = 0,
    val isAutoApproved: Boolean = false,
    val tempToken: String? = null,
    
    // Phone Verification
    val phoneNumber: String = "",
    val isPhoneAdded: Boolean = false,
    val otpCode: String = "",
    val isOtpVerified: Boolean = false,
    
    // PIN Creation
    val pin: String = "",
    val confirmPin: String = "",
    val isPinCreated: Boolean = false,
    
    // Final State
    val isRegistrationComplete: Boolean = false
)

// KYC Events
sealed class KycEvent {
    object NavigateToNext : KycEvent()
    object NavigateBack : KycEvent() 
    object NavigateToLogin : KycEvent()
    object RegistrationComplete : KycEvent()
    data class ShowError(val message: String) : KycEvent()
    data class ShowSuccess(val message: String) : KycEvent()
}

class KycViewModel(
    private val registerWithIdUseCase: RegisterWithIdUseCase,
    private val addPhoneUseCase: AddPhoneUseCase,
    private val createPinKycUseCase: CreatePinKycUseCase,
    private val authRepository: AuthRepository,
    private val sessionStorage: com.example.g_kash.data.SessionStorage,
    private val sendOtpUseCase: com.example.g_kash.otp.domain.SendOtpUseCase,
    private val verifyOtpUseCase: com.example.g_kash.otp.domain.VerifyOtpUseCase
) : ViewModel() {

    private val _uiState = mutableStateOf(KycUiState())
    val uiState: State<KycUiState> = _uiState

    private val _events = MutableSharedFlow<KycEvent>()
    val events: SharedFlow<KycEvent> = _events.asSharedFlow()

    // Utility function to convert Uri to ByteArray
    private fun uriToByteArray(context: Context, uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            Log.e("KYC", "Error converting Uri to ByteArray", e)
            null
        }
    }

    // Step 1: Upload ID and Selfie using real API
    fun uploadIdAndSelfie(context: Context, idImageUri: Uri, selfieUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // DEBUG: Enable mock mode for testing navigation
            val debugMode = false
            
            if (debugMode) {
                // Mock successful response for testing navigation
                kotlinx.coroutines.delay(2000) // Simulate processing time
                
                val mockData = ExtractedIdData(
                    user_name = "Test User",
                    user_nationalId = "12345678",
                    dateOfBirth = "01.01.1990"
                )
                
                val newState = _uiState.value.copy(
                    isLoading = false,
                    idImageUri = idImageUri,
                    selfieUri = selfieUri,
                    extractedData = mockData,
                    verificationScore = 100,
                    isAutoApproved = true,
                    tempToken = "mock_token_12345",
                    currentStep = KycStep.ADD_PHONE,
                    progress = 0.33f
                )
                _uiState.value = newState
                
                // Save mock token to session storage
                sessionStorage.saveAuthToken("mock_token_12345")
                
                Log.d("KYC", "DEBUG: Mock ID processed successfully: ${mockData.user_name}")
                Log.d("KYC", "DEBUG: Current step set to: ${_uiState.value.currentStep}")
                Log.d("KYC", "DEBUG: Mock token saved to session storage")
                _events.emit(KycEvent.NavigateToNext)
                _events.emit(KycEvent.ShowSuccess("ID verified! Please add your phone number."))
                return@launch
            }

            // Convert URIs to ByteArray
            val idImageData = uriToByteArray(context, idImageUri)
            val selfieData = uriToByteArray(context, selfieUri)
            
            if (idImageData == null || selfieData == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to process images. Please try again."
                )
                _events.emit(KycEvent.ShowError("Failed to process images. Please try again."))
                return@launch
            }

            try {
                // Call real API with timeout (60 seconds for image processing)
                val result = kotlinx.coroutines.withTimeout(60000L) { // 60 second timeout
                    registerWithIdUseCase(idImageData, selfieData)
                }
                
                result.fold(
                    onSuccess = { response ->
                        if (response.success) {
                            // Create new state explicitly to ensure recomposition
                            val newState = _uiState.value.copy(
                                isLoading = false,
                                idImageUri = idImageUri,
                                selfieUri = selfieUri,
                                extractedData = response.extractedData,
                                verificationScore = response.score,
                                isAutoApproved = response.verified,
                                tempToken = response.temp_token,
                                currentStep = KycStep.ADD_PHONE,
                                progress = 0.33f
                            )
                            _uiState.value = newState
                            Log.d("KYC", "ID processed successfully: ${response.extractedData?.user_name}")
                            Log.d("KYC", "Current step set to: ${_uiState.value.currentStep}")
                            _events.emit(KycEvent.NavigateToNext)
                            _events.emit(KycEvent.ShowSuccess("ID verified! Please add your phone number."))
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = response.message
                            )
                            Log.e("KYC", "ID processing failed: ${response.message}")
                            _events.emit(KycEvent.ShowError(response.message))
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to process ID images"
                        )
                        Log.e("KYC", "ID processing failed", error)
                        _events.emit(KycEvent.ShowError(error.message ?: "Failed to process ID images"))
                    }
                )
            } catch (timeout: kotlinx.coroutines.TimeoutCancellationException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Upload is taking longer than expected. The server might be busy. Please try again."
                )
                Log.e("KYC", "ID upload timeout", timeout)
                _events.emit(KycEvent.ShowError("Upload is taking longer than expected. The server might be busy. Please try again."))
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is io.ktor.client.plugins.ServerResponseException -> {
                        when (e.response.status.value) {
                            500 -> "Server is experiencing issues. Please try again in a few minutes."
                            502 -> "Server is temporarily unavailable. Please try again in a few minutes."
                            else -> "Server error: ${e.response.status.value}. Please try again."
                        }
                    }
                    else -> "Network error: ${e.message}"
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
                Log.e("KYC", "Network error", e)
                _events.emit(KycEvent.ShowError(errorMessage))
            }
        }
    }
    
    // Step 2: Add phone number using real API
    fun addPhoneNumber(phoneNumber: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val result = kotlinx.coroutines.withTimeout(15000L) { // 15 second timeout
                    addPhoneUseCase(phoneNumber)
                }
                
                result.fold(
                    onSuccess = { response ->
                        if (response.success) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                phoneNumber = phoneNumber,
                                isPhoneAdded = true,
                                currentStep = KycStep.VERIFY_PHONE,
                                progress = 0.50f
                            )
                            Log.d("KYC", "Phone number added successfully: $phoneNumber")
                            _events.emit(KycEvent.NavigateToNext)
                            _events.emit(KycEvent.ShowSuccess("Phone number added! Please enter the verification code."))
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = response.message
                            )
                            Log.e("KYC", "Phone addition failed: ${response.message}")
                            _events.emit(KycEvent.ShowError(response.message))
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to add phone number"
                        )
                        Log.e("KYC", "Phone addition failed", error)
                        _events.emit(KycEvent.ShowError(error.message ?: "Failed to add phone number"))
                    }
                )
            } catch (timeout: kotlinx.coroutines.TimeoutCancellationException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Request timed out. Please try again."
                )
                Log.e("KYC", "Phone addition timeout", timeout)
                _events.emit(KycEvent.ShowError("Request timed out. Please try again."))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Network error: ${e.message}"
                )
                Log.e("KYC", "Network error", e)
                _events.emit(KycEvent.ShowError("Network error: ${e.message}"))
            }
        }
    }

    // Step 3: Verify OTP using real OTP service
    fun verifyOtp(otp: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val phoneNumber = _uiState.value.phoneNumber
            if (phoneNumber.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Phone number not found. Please restart the process."
                )
                _events.emit(KycEvent.ShowError("Phone number not found. Please restart the process."))
                return@launch
            }

            verifyOtpUseCase(phoneNumber, otp).fold(
                onSuccess = { verifyResponse ->
                    if (verifyResponse.success) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            otpCode = otp,
                            isOtpVerified = true,
                            currentStep = KycStep.CREATE_PIN,
                            progress = 0.67f
                        )
                        Log.d("KYC", "OTP verified successfully")
                        _events.emit(KycEvent.NavigateToNext)
                        _events.emit(KycEvent.ShowSuccess("Phone number verified successfully"))
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = verifyResponse.message
                        )
                        Log.e("KYC", "OTP verification failed: ${verifyResponse.message}")
                        _events.emit(KycEvent.ShowError(verifyResponse.message))
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to verify OTP"
                    )
                    Log.e("KYC", "OTP verification failed", error)
                    _events.emit(KycEvent.ShowError(error.message ?: "Failed to verify OTP"))
                }
            )
        }
    }

    // Step 4: Create PIN
    fun createPin(pin: String) {
        if (pin.length != 4) {
            _uiState.value = _uiState.value.copy(error = "PIN must be 4 digits")
            return
        }

        _uiState.value = _uiState.value.copy(
            pin = pin,
            currentStep = KycStep.CONFIRM_PIN,
            progress = 0.83f,
            error = null
        )
        
        viewModelScope.launch {
            _events.emit(KycEvent.NavigateToNext)
        }
    }

    // Step 4: Confirm PIN and complete registration using real API
    fun confirmPin(confirmPin: String) {
        val currentPin = _uiState.value.pin
        
        if (confirmPin != currentPin) {
            _uiState.value = _uiState.value.copy(error = "PINs do not match")
            viewModelScope.launch {
                _events.emit(KycEvent.ShowError("PINs do not match"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val result = kotlinx.coroutines.withTimeout(15000L) { // 15 second timeout
                    createPinKycUseCase(currentPin)
                }
                
                result.fold(
                    onSuccess = { response ->
                        if (response.success) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                confirmPin = confirmPin,
                                isPinCreated = true,
                                isRegistrationComplete = true,
                                currentStep = KycStep.COMPLETE,
                                progress = 1.0f
                            )
                            Log.d("KYC", "Registration completed successfully")
                            _events.emit(KycEvent.RegistrationComplete)
                            _events.emit(KycEvent.ShowSuccess("Registration completed successfully!"))
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = response.message
                            )
                            Log.e("KYC", "PIN creation failed: ${response.message}")
                            _events.emit(KycEvent.ShowError(response.message))
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to create PIN"
                        )
                        Log.e("KYC", "PIN creation failed", error)
                        _events.emit(KycEvent.ShowError(error.message ?: "Failed to create PIN"))
                    }
                )
            } catch (timeout: kotlinx.coroutines.TimeoutCancellationException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Request timed out. Please try again."
                )
                Log.e("KYC", "PIN creation timeout", timeout)
                _events.emit(KycEvent.ShowError("Request timed out. Please try again."))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Network error: ${e.message}"
                )
                Log.e("KYC", "Network error", e)
                _events.emit(KycEvent.ShowError("Network error: ${e.message}"))
            }
        }
    }

    // Start KYC flow with ID upload
    fun startKyc() {
        _uiState.value = _uiState.value.copy(
            currentStep = KycStep.UPLOAD_ID,
            progress = 0.17f
        )
        
        viewModelScope.launch {
            _events.emit(KycEvent.NavigateToNext)
        }
    }

    // Navigation helpers - proper flow without manual entry
    fun goBack() {
        val currentStep = _uiState.value.currentStep
        val newStep = when (currentStep) {
            KycStep.UPLOAD_ID -> KycStep.WELCOME
            KycStep.ADD_PHONE -> KycStep.UPLOAD_ID
            KycStep.VERIFY_PHONE -> KycStep.ADD_PHONE
            KycStep.CREATE_PIN -> KycStep.VERIFY_PHONE
            KycStep.CONFIRM_PIN -> KycStep.CREATE_PIN
            else -> currentStep
        }
        
        val newProgress = when (newStep) {
            KycStep.WELCOME -> 0.0f
            KycStep.UPLOAD_ID -> 0.17f
            KycStep.ADD_PHONE -> 0.33f
            KycStep.VERIFY_PHONE -> 0.50f
            KycStep.CREATE_PIN -> 0.67f
            KycStep.CONFIRM_PIN -> 0.83f
            else -> _uiState.value.progress
        }

        _uiState.value = _uiState.value.copy(
            currentStep = newStep,
            progress = newProgress,
            error = null
        )
        
        viewModelScope.launch {
            _events.emit(KycEvent.NavigateBack)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun navigateToLogin() {
        viewModelScope.launch {
            _events.emit(KycEvent.NavigateToLogin)
        }
    }

    // Utility functions for UI
    fun getStepTitle(): String {
        return when (_uiState.value.currentStep) {
            KycStep.WELCOME -> "Welcome to G-Kash!"
            KycStep.UPLOAD_ID -> "Upload Your ID"
            KycStep.ADD_PHONE -> "Add Phone Number"
            KycStep.VERIFY_PHONE -> "Verify Phone Number"
            KycStep.CREATE_PIN -> "Create PIN"
            KycStep.CONFIRM_PIN -> "Confirm PIN"
            KycStep.COMPLETE -> "Registration Complete"
            else -> ""
        }
    }

    fun getStepDescription(): String {
        return when (_uiState.value.currentStep) {
            KycStep.WELCOME -> "Let's get you set up securely in just a few minutes."
            KycStep.UPLOAD_ID -> "Please upload a clear photo of your government-issued ID and a selfie"
            KycStep.ADD_PHONE -> "Enter your phone number for verification"
            KycStep.VERIFY_PHONE -> "Enter the verification code sent to your phone"
            KycStep.CREATE_PIN -> "Create a 4-digit PIN for secure access"
            KycStep.CONFIRM_PIN -> "Re-enter your PIN to confirm"
            KycStep.COMPLETE -> "Your account has been created successfully"
            else -> ""
        }
    }

    fun getProgressText(): String {
        val step = when (_uiState.value.currentStep) {
            KycStep.WELCOME -> "Welcome"
            KycStep.UPLOAD_ID -> "1 of 6"
            KycStep.ADD_PHONE -> "2 of 6"
            KycStep.VERIFY_PHONE -> "3 of 6"
            KycStep.CREATE_PIN -> "4 of 6"
            KycStep.CONFIRM_PIN -> "5 of 6"
            KycStep.COMPLETE -> "6 of 6"
            else -> "Welcome"
        }
        return if (_uiState.value.currentStep == KycStep.WELCOME) step else "Step $step"
    }

    fun getProgressPercentage(): String {
        return "${(_uiState.value.progress * 100).toInt()}%"
    }
}