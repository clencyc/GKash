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

    // Step 1: Upload ID and Selfie for verification
    fun uploadIdAndSelfie(context: Context, idImageUri: Uri, selfieUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val idBytes = uriToByteArray(context, idImageUri)
            val selfieBytes = uriToByteArray(context, selfieUri)

            if (idBytes == null || selfieBytes == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to process images. Please try again."
                )
                return@launch
            }

            registerWithIdUseCase(idBytes, selfieBytes).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        idImageUri = idImageUri,
                        selfieUri = selfieUri,
                        extractedData = response.extractedData,
                        verificationScore = response.score,
                        isAutoApproved = response.verified,
                        currentStep = KycStep.ADD_PHONE,
                        progress = 0.33f // Updated progress for 6-step process
                    )
                    Log.d("KYC", "ID verification successful: ${response.extractedData.user_name}")
                    _events.emit(KycEvent.NavigateToNext)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to verify ID. Please try again."
                    )
                    Log.e("KYC", "ID verification failed", error)
                    _events.emit(KycEvent.ShowError(error.message ?: "ID verification failed"))
                }
            )
        }
    }

    // Step 2: Add phone number and send OTP (Demo mode - skip backend registration)
    fun addPhoneNumber(phoneNumber: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Demo mode: Skip backend phone registration and go directly to OTP
            Log.d("KYC", "Demo mode: Skipping backend phone registration")
            
            // Send real OTP directly
            val userName = _uiState.value.extractedData?.user_name ?: "Demo User"
            sendOtpUseCase(phoneNumber, userName).fold(
                onSuccess = { otpResponse ->
                    if (otpResponse.success) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            phoneNumber = phoneNumber,
                            isPhoneAdded = true,
                            currentStep = KycStep.VERIFY_PHONE,
                            progress = 0.50f
                        )
                        Log.d("KYC", "OTP sent successfully to $phoneNumber (Demo mode)")
                        _events.emit(KycEvent.NavigateToNext)
                        _events.emit(KycEvent.ShowSuccess("Verification code sent to $phoneNumber"))
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = otpResponse.message
                        )
                        Log.e("KYC", "OTP send failed: ${otpResponse.message}")
                        _events.emit(KycEvent.ShowError(otpResponse.message))
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to send verification code"
                    )
                    Log.e("KYC", "OTP send failed", error)
                    _events.emit(KycEvent.ShowError(error.message ?: "Failed to send verification code"))
                }
            )
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

    // Step 5: Confirm PIN and complete registration (Demo mode - simplified)
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

            // Demo mode: Skip backend PIN creation, just simulate success
            Log.d("KYC", "Demo mode: Skipping backend PIN creation")
            
            // Simulate processing delay
            kotlinx.coroutines.delay(1500)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                confirmPin = confirmPin,
                isPinCreated = true,
                isRegistrationComplete = true,
                currentStep = KycStep.COMPLETE,
                progress = 1.0f
            )
            
            Log.d("KYC", "Demo mode: Registration completed successfully")
            _events.emit(KycEvent.RegistrationComplete)
            _events.emit(KycEvent.ShowSuccess("Demo registration completed successfully!"))
        }
    }

    // Welcome step navigation - temporarily skip ID verification for demo
    fun startKyc() {
        // Skip ID verification and go directly to phone verification
        // Set mock extracted data for demo purposes
        val mockExtractedData = com.example.g_kash.authentication.data.ExtractedIdData(
            user_name = "Demo User",
            user_nationalId = "12345678",
            dateOfBirth = "1990-01-01"
        )
        
        _uiState.value = _uiState.value.copy(
            currentStep = KycStep.ADD_PHONE,
            progress = 0.33f,
            extractedData = mockExtractedData,
            isAutoApproved = true // Mock as auto-approved for demo
        )
        
        viewModelScope.launch {
            _events.emit(KycEvent.ShowSuccess("Demo mode: ID verification skipped"))
        }
    }

    // Navigation helpers - adjusted for demo mode (skip ID steps)
    fun goBack() {
        val currentStep = _uiState.value.currentStep
        val newStep = when (currentStep) {
            KycStep.UPLOAD_ID -> KycStep.WELCOME
            KycStep.ADD_PHONE -> KycStep.WELCOME // Skip back to welcome in demo mode
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