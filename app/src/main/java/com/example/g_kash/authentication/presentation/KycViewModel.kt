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

    // Step 1: Upload ID and Selfie (No API call - just store images)
    fun uploadIdAndSelfie(context: Context, idImageUri: Uri, selfieUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Simulate processing time
            kotlinx.coroutines.delay(2000)

            // Store images and move to manual data entry
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                idImageUri = idImageUri,
                selfieUri = selfieUri,
                verificationScore = 95,
                currentStep = KycStep.MANUAL_ENTRY,
                progress = 0.25f
            )
            
            Log.d("KYC", "ID and selfie uploaded successfully - ready for manual entry")
            _events.emit(KycEvent.NavigateToNext)
            _events.emit(KycEvent.ShowSuccess("Images uploaded! Please enter your details."))
        }
    }
    
    // Step 1.5: Manual entry of ID details
    fun submitManualIdData(name: String, nationalId: String, dateOfBirth: String) {
        if (name.isBlank() || nationalId.isBlank() || dateOfBirth.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please fill in all fields")
            return
        }
        
        val extractedData = com.example.g_kash.authentication.data.ExtractedIdData(
            user_name = name.trim(),
            user_nationalId = nationalId.trim(),
            dateOfBirth = dateOfBirth
        )
        
        _uiState.value = _uiState.value.copy(
            extractedData = extractedData,
            isAutoApproved = true,
            currentStep = KycStep.ADD_PHONE,
            progress = 0.33f,
            error = null
        )
        
        viewModelScope.launch {
            _events.emit(KycEvent.NavigateToNext)
            _events.emit(KycEvent.ShowSuccess("Details saved successfully!"))
        }
    }

    // Step 2: Add phone number and send OTP (Keep real OTP service)
    fun addPhoneNumber(phoneNumber: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Send real OTP using the existing OTP service
            val userName = _uiState.value.extractedData?.user_name ?: "User"
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
                        Log.d("KYC", "OTP sent successfully to $phoneNumber")
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

    // Step 5: Confirm PIN and complete registration
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
            
            Log.d("KYC", "Registration completed successfully")
            _events.emit(KycEvent.RegistrationComplete)
            _events.emit(KycEvent.ShowSuccess("Registration completed successfully!"))
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

    // Navigation helpers - proper flow with ID upload and manual entry
    fun goBack() {
        val currentStep = _uiState.value.currentStep
        val newStep = when (currentStep) {
            KycStep.UPLOAD_ID -> KycStep.WELCOME
            KycStep.MANUAL_ENTRY -> KycStep.UPLOAD_ID
            KycStep.ADD_PHONE -> KycStep.MANUAL_ENTRY
            KycStep.VERIFY_PHONE -> KycStep.ADD_PHONE
            KycStep.CREATE_PIN -> KycStep.VERIFY_PHONE
            KycStep.CONFIRM_PIN -> KycStep.CREATE_PIN
            else -> currentStep
        }
        
        val newProgress = when (newStep) {
            KycStep.WELCOME -> 0.0f
            KycStep.UPLOAD_ID -> 0.17f
            KycStep.MANUAL_ENTRY -> 0.25f
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
            KycStep.MANUAL_ENTRY -> "Enter Your Details"
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
            KycStep.MANUAL_ENTRY -> "Please enter the details from your ID document"
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
            KycStep.UPLOAD_ID -> "1 of 7"
            KycStep.MANUAL_ENTRY -> "2 of 7"
            KycStep.ADD_PHONE -> "3 of 7"
            KycStep.VERIFY_PHONE -> "4 of 7"
            KycStep.CREATE_PIN -> "5 of 7"
            KycStep.CONFIRM_PIN -> "6 of 7"
            KycStep.COMPLETE -> "7 of 7"
            else -> "Welcome"
        }
        return if (_uiState.value.currentStep == KycStep.WELCOME) step else "Step $step"
    }

    fun getProgressPercentage(): String {
        return "${(_uiState.value.progress * 100).toInt()}%"
    }
}