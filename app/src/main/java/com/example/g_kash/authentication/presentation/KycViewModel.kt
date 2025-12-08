package com.example.g_kash.authentication.presentation

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.analytics.AnalyticsHelper
import com.example.g_kash.authentication.data.ExtractedIdData
import com.example.g_kash.authentication.domain.*
import com.example.g_kash.authentication.presentation.KycStep
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// KYC UI State
data class KycUiState(
    val currentStep: KycStep = KycStep.WELCOME,
    val isLoading: Boolean = false,
    val error: String? = null,
    val progress: Float = 0.0f,
    
    // Account Creation
    val fullName: String = "",
    val email: String = "",
    val isAccountCreated: Boolean = false,
    
    // Phone Verification
    val phoneNumber: String = "",
    val isPhoneAdded: Boolean = false,
    val otpCode: String = "",
    val isOtpVerified: Boolean = false,
    
    // PIN Creation
    val pin: String = "",
    val confirmPin: String = "",
    val isPinCreated: Boolean = false,
    
    // Token for API calls
    val tempToken: String = "",
    
    // Final State
    val isRegistrationComplete: Boolean = false
)

// KYC Events
sealed class KycEvent {
    object NavigateToNext : KycEvent()
    object NavigateBack : KycEvent() 
    object NavigateToLogin : KycEvent()
    object RegistrationComplete : KycEvent()
    data class NavigateToDashboard(val token: String) : KycEvent()
    data class ShowError(val message: String) : KycEvent()
    data class ShowSuccess(val message: String) : KycEvent()
}

class KycViewModel(
    private val addPhoneUseCase: AddPhoneUseCase,
    private val createPinKycUseCase: CreatePinKycUseCase,
    private val authRepository: AuthRepository,
    private val sessionStorage: com.example.g_kash.data.SessionStorage,
    private val sendOtpUseCase: com.example.g_kash.otp.domain.SendOtpUseCase,
    private val verifyOtpUseCase: com.example.g_kash.otp.domain.VerifyOtpUseCase,
    private val createAccountUseCase: com.example.g_kash.authentication.domain.CreateAccountUseCase,
    private val firebaseAnalytics: FirebaseAnalytics
) : ViewModel() {

    private val _uiState = mutableStateOf(KycUiState())
    val uiState: State<KycUiState> = _uiState

    private val _events = MutableSharedFlow<KycEvent>()
    val events: SharedFlow<KycEvent> = _events.asSharedFlow()

    // Step 1: Create account with email, fullName, and PIN
    fun createAccount(fullName: String, email: String, pin: String, confirmPin: String) {
        Log.d("KYC_REG", "============================================")
        Log.d("KYC_REG", "STARTING ACCOUNT CREATION")
        Log.d("KYC_REG", "Full Name: $fullName")
        Log.d("KYC_REG", "Email: $email")
        Log.d("KYC_REG", "PIN length: ${pin.length}")
        Log.d("KYC_REG", "Confirm PIN length: ${confirmPin.length}")
        Log.d("KYC_REG", "============================================")
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                fullName = fullName,
                email = email,
                pin = pin,
                confirmPin = confirmPin
            )
            
            Log.d("KYC_REG", "UI state updated - isLoading: true")
            
            // Show user-friendly message
            _events.emit(KycEvent.ShowSuccess("Creating your account... Please wait."))
            
            try {
                Log.d("KYC_REG", "Calling createAccountUseCase...")
                val result = createAccountUseCase(fullName, email, pin, confirmPin)
                Log.d("KYC_REG", "Received result from createAccountUseCase")
                
                result.fold(
                    onSuccess = { response ->
                        Log.d("KYC_REG", "SUCCESS - Response received")
                        Log.d("KYC_REG", "success: ${response.success}")
                        Log.d("KYC_REG", "message: ${response.message}")
                        Log.d("KYC_REG", "token: ${if (response.token != null) "Present (${response.token.take(20)}...)" else "NULL"}")
                        Log.d("KYC_REG", "user: ${response.user}")
                        
                        if (response.success && response.token != null) {
                            // Store the token from account creation
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isAccountCreated = true,
                                tempToken = response.token,
                                currentStep = KycStep.ADD_PHONE,
                                progress = 0.40f
                            )
                            Log.d("KYC_REG", "✓ Account created successfully, navigating to ADD_PHONE")
                            
                            // Analytics: Log account creation
                            AnalyticsHelper.logEvent(
                                AnalyticsHelper.Events.ACCOUNT_CREATED,
                                mapOf("email" to email),
                                firebaseAnalytics
                            )
                            AnalyticsHelper.setUserProperty(
                                AnalyticsHelper.UserProperties.REGISTRATION_STAGE,
                                "phone_verification",
                                firebaseAnalytics
                            )
                            
                            _events.emit(KycEvent.NavigateToNext)
                            _events.emit(KycEvent.ShowSuccess("Account created! Now add your phone number."))
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = response.message ?: "Failed to create account"
                            )
                            Log.e("KYC_REG", "✗ Account creation failed: ${response.message}")
                            
                            // Analytics: Log error
                            AnalyticsHelper.logEvent(
                                AnalyticsHelper.Events.ERROR_OCCURRED,
                                mapOf("stage" to "account_creation", "error" to (response.message ?: "Unknown error")),
                                firebaseAnalytics
                            )
                            
                            _events.emit(KycEvent.ShowError(response.message ?: "Failed to create account"))
                        }
                    },
                    onFailure = { error ->
                        Log.e("KYC_REG", "✗ FAILURE - Error occurred", error)
                        Log.e("KYC_REG", "Error type: ${error::class.simpleName}")
                        Log.e("KYC_REG", "Error message: ${error.message}")
                        Log.e("KYC_REG", "Stack trace:", error)
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to create account"
                        )
                        _events.emit(KycEvent.ShowError(error.message ?: "Failed to create account"))
                    }
                )
            } catch (e: Exception) {
                Log.e("KYC_REG", "✗ EXCEPTION in createAccount", e)
                Log.e("KYC_REG", "Exception type: ${e::class.simpleName}")
                Log.e("KYC_REG", "Exception message: ${e.message}")
                Log.e("KYC_REG", "Stack trace:", e)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create account"
                )
                _events.emit(KycEvent.ShowError(e.message ?: "Failed to create account"))
            }
        }
        Log.d("KYC_REG", "createAccount coroutine launched")
    }

    // Step 2: Add phone number and send OTP using Tiara Connect
    fun addPhoneNumber(phoneNumber: String) {
        Log.d("KYC_OTP", "============================================")
        Log.d("KYC_OTP", "STARTING OTP SEND PROCESS")
        Log.d("KYC_OTP", "Phone Number: $phoneNumber")
        Log.d("KYC_OTP", "User Name: ${_uiState.value.fullName}")
        Log.d("KYC_OTP", "============================================")
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, phoneNumber = phoneNumber)
            Log.d("KYC_OTP", "UI state updated - isLoading: true")
            
            // Show user-friendly message about potential delay
            _events.emit(KycEvent.ShowSuccess("Sending verification code... Please wait."))
            Log.d("KYC_OTP", "User message emitted")
            
            try {
                // Send OTP directly using Tiara Connect API
                Log.d("KYC_OTP", "Calling sendOtpUseCase...")
                val result = sendOtpUseCase(
                    phoneNumber = phoneNumber,
                    userName = _uiState.value.fullName
                )
                Log.d("KYC_OTP", "Received result from sendOtpUseCase")
                
                result.fold(
                    onSuccess = { response ->
                        Log.d("KYC_OTP", "SUCCESS - Response received")
                        Log.d("KYC_OTP", "success: ${response.success}")
                        Log.d("KYC_OTP", "message: ${response.message}")
                        
                        if (response.success) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isPhoneAdded = true,
                                currentStep = KycStep.VERIFY_PHONE,
                                progress = 0.66f
                            )
                            Log.d("KYC_OTP", "✓ OTP sent successfully to: $phoneNumber")
                            
                            // Analytics: Log phone added and OTP sent
                            AnalyticsHelper.logEvent(
                                AnalyticsHelper.Events.PHONE_ADDED,
                                mapOf("phone" to phoneNumber.takeLast(4)),
                                firebaseAnalytics
                            )
                            AnalyticsHelper.logEvent(
                                AnalyticsHelper.Events.OTP_SENT,
                                mapOf("phone" to phoneNumber.takeLast(4)),
                                firebaseAnalytics
                            )
                            
                            _events.emit(KycEvent.NavigateToNext)
                            _events.emit(KycEvent.ShowSuccess("Verification code sent! Please check your SMS."))
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = response.message ?: "Failed to send verification code"
                            )
                            Log.e("KYC_OTP", "✗ OTP sending failed: ${response.message}")
                            _events.emit(KycEvent.ShowError(response.message ?: "Failed to send verification code"))
                        }
                    },
                    onFailure = { error ->
                        Log.e("KYC_OTP", "✗ FAILURE - Error occurred", error)
                        Log.e("KYC_OTP", "Error type: ${error::class.simpleName}")
                        Log.e("KYC_OTP", "Error message: ${error.message}")
                        Log.e("KYC_OTP", "Stack trace:", error)
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to send verification code"
                        )
                        _events.emit(KycEvent.ShowError(error.message ?: "Failed to send verification code. Please try again."))
                    }
                )
            } catch (e: Exception) {
                Log.e("KYC_OTP", "✗ EXCEPTION in addPhoneNumber", e)
                Log.e("KYC_OTP", "Exception type: ${e::class.simpleName}")
                Log.e("KYC_OTP", "Exception message: ${e.message}")
                Log.e("KYC_OTP", "Stack trace:", e)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to send verification code"
                )
                _events.emit(KycEvent.ShowError(e.message ?: "Failed to send verification code. Please try again."))
            }
        }
        Log.d("KYC_OTP", "addPhoneNumber coroutine launched")
    }

    // Step 3: Verify OTP using real OTP service
    fun verifyOtp(otp: String) {
        Log.d("KYC_VERIFY", "============================================")
        Log.d("KYC_VERIFY", "STARTING OTP VERIFICATION")
        Log.d("KYC_VERIFY", "OTP: $otp")
        Log.d("KYC_VERIFY", "Phone: ${_uiState.value.phoneNumber}")
        Log.d("KYC_VERIFY", "============================================")
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            Log.d("KYC_VERIFY", "UI state updated - isLoading: true")

            val phoneNumber = _uiState.value.phoneNumber
            if (phoneNumber.isBlank()) {
                Log.e("KYC_VERIFY", "✗ Phone number is blank!")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Phone number not found. Please restart the process."
                )
                _events.emit(KycEvent.ShowError("Phone number not found. Please restart the process."))
                return@launch
            }

            Log.d("KYC_VERIFY", "Calling verifyOtpUseCase...")
            verifyOtpUseCase(phoneNumber, otp).fold(
                onSuccess = { verifyResponse ->
                    Log.d("KYC_VERIFY", "SUCCESS - Response received")
                    Log.d("KYC_VERIFY", "success: ${verifyResponse.success}")
                    Log.d("KYC_VERIFY", "message: ${verifyResponse.message}")
                    
                    if (verifyResponse.success) {
                        val token = _uiState.value.tempToken
                        val fullName = _uiState.value.fullName
                        val email = _uiState.value.email
                        val userId = _uiState.value.phoneNumber
                        
                        Log.d("KYC_VERIFY", "✓ OTP verified successfully")
                        Log.d("KYC_VERIFY", "Token: ${token.take(20)}...")
                        Log.d("KYC_VERIFY", "Full Name: $fullName")
                        Log.d("KYC_VERIFY", "Email: $email")
                        Log.d("KYC_VERIFY", "User ID: $userId")
                        
                        // CRITICAL: Save session FIRST before any navigation or state changes
                        try {
                            Log.d("KYC_VERIFY", "Saving session to SessionStorage...")
                            authRepository.saveSessionAfterKyc(
                                token = token,
                                userId = userId,
                                userName = fullName,
                                userEmail = email
                            )
                            Log.d("KYC_VERIFY", "✓ Session saved successfully to SessionStorage")
                            
                            // Wait 500ms for DataStore to propagate the token
                            kotlinx.coroutines.delay(500)
                            Log.d("KYC_VERIFY", "Token propagation delay completed")
                            
                            // Verify token was actually saved
                            val savedToken = authRepository.getAuthTokenStream().first()
                            Log.d("KYC_VERIFY", "Verified saved token: ${if (savedToken != null) "Present (${savedToken.take(20)}...)" else "NULL"}")
                        } catch (e: Exception) {
                            Log.e("KYC_VERIFY", "✗ CRITICAL: Failed to save session", e)
                            Log.e("KYC_VERIFY", "Exception type: ${e::class.simpleName}")
                            Log.e("KYC_VERIFY", "Exception message: ${e.message}")
                            
                            _uiState.value = _uiState.value.copy(isLoading = false)
                            _events.emit(KycEvent.ShowError("Failed to save session: ${e.message}"))
                            return@fold
                        }
                        
                        // NOW update UI state
                        Log.d("KYC_VERIFY", "Updating UI state...")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            otpCode = otp,
                            isOtpVerified = true,
                            currentStep = KycStep.COMPLETE,
                            progress = 1.0f,
                            isRegistrationComplete = true,
                            isPinCreated = true
                        )
                        Log.d("KYC_VERIFY", "✓ UI state updated - registration complete")
                        
                        // Analytics: Log OTP verification and registration completion
                        AnalyticsHelper.logEvent(
                            AnalyticsHelper.Events.OTP_VERIFIED,
                            mapOf("phone" to phoneNumber.takeLast(4)),
                            firebaseAnalytics
                        )
                        AnalyticsHelper.logEvent(
                            AnalyticsHelper.Events.REGISTRATION_COMPLETED,
                            mapOf("email" to email, "phone" to phoneNumber.takeLast(4)),
                            firebaseAnalytics
                        )
                        AnalyticsHelper.setUserProperty(
                            AnalyticsHelper.UserProperties.REGISTRATION_STAGE,
                            "completed",
                            firebaseAnalytics
                        )
                        
                        // Emit success message
                        _events.emit(KycEvent.ShowSuccess("Registration completed successfully!"))
                        Log.d("KYC_VERIFY", "Success message emitted")
                        
                        // FINALLY navigate (token is guaranteed to be in SessionStorage)
                        Log.d("KYC_VERIFY", "Emitting NavigateToDashboard event")
                        _events.emit(KycEvent.NavigateToDashboard(token = token))
                        Log.d("KYC_VERIFY", "============================================")
                        Log.d("KYC_VERIFY", "REGISTRATION COMPLETE - Navigating to Dashboard")
                        Log.d("KYC_VERIFY", "============================================")
                    } else {
                        Log.e("KYC_VERIFY", "✗ OTP verification failed: ${verifyResponse.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = verifyResponse.message
                        )
                        _events.emit(KycEvent.ShowError(verifyResponse.message))
                    }
                },
                onFailure = { error ->
                    Log.e("KYC_VERIFY", "✗ FAILURE - Error occurred", error)
                    Log.e("KYC_VERIFY", "Error type: ${error::class.simpleName}")
                    Log.e("KYC_VERIFY", "Error message: ${error.message}")
                    Log.e("KYC_VERIFY", "Stack trace:", error)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to verify OTP"
                    )
                    _events.emit(KycEvent.ShowError(error.message ?: "Failed to verify OTP"))
                }
            )
        }
        Log.d("KYC_VERIFY", "verifyOtp coroutine launched")
    }

    // Step 4: Create PIN
    fun createPin(pin: String) {
        _uiState.value = _uiState.value.copy(pin = pin)
    }

    // Step 4: Confirm PIN and complete registration using real API
    fun confirmPin(confirmPin: String) {
        viewModelScope.launch {
            // Capture state values at the start to avoid scope issues
            val userFullName = _uiState.value.fullName
            val userEmail = _uiState.value.email
            val userTempToken = _uiState.value.tempToken
            val userPin = _uiState.value.pin
            
            if (userPin != confirmPin) {
                _uiState.value = _uiState.value.copy(
                    error = "PINs do not match. Please try again."
                )
                _events.emit(KycEvent.ShowError("PINs do not match. Please try again."))
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val result = createPinKycUseCase(
                    CreatePinKycUseCase.Params(
                        pin = userPin,
                        tempToken = userTempToken
                    )
                )
                
                result.fold(
                    onSuccess = { response ->
                        if (response.success) {
                            // Capture current state values before launching nested coroutine
                            val savedToken = response.token ?: userTempToken
                            val savedUserId = response.user?.id ?: ""
                            val savedUserName = response.user?.user_name ?: userFullName
                            val savedUserEmail = response.user?.email ?: userEmail
                            
                            // Save the session now that KYC is complete
                            viewModelScope.launch {
                                try {
                                    authRepository.saveSessionAfterKyc(
                                        token = savedToken,
                                        userId = savedUserId,
                                        userName = savedUserName,
                                        userEmail = savedUserEmail
                                    )
                                    Log.d("KYC", "Session saved after KYC completion")
                                } catch (e: Exception) {
                                    Log.e("KYC", "Failed to save session after KYC", e)
                                }
                            }
                            
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isPinCreated = true,
                                currentStep = KycStep.COMPLETE,
                                progress = 1.0f,
                                isRegistrationComplete = true
                            )
                            Log.d("KYC", "Registration completed successfully, navigating to dashboard with token")
                            _events.emit(KycEvent.ShowSuccess("Registration completed successfully!"))
                            _events.emit(KycEvent.NavigateToDashboard(token = savedToken))
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = response.message ?: "Failed to create PIN"
                            )
                            Log.e("KYC", "PIN creation failed: ${response.message}")
                            _events.emit(KycEvent.ShowError(response.message ?: "Failed to create PIN"))
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
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create PIN"
                )
                Log.e("KYC", "PIN creation failed", e)
                _events.emit(KycEvent.ShowError(e.message ?: "Failed to create PIN"))
            }
        }
    }

    // Start KYC flow with account creation
    fun startKyc() {
        _uiState.value = _uiState.value.copy(
            currentStep = KycStep.CREATE_ACCOUNT,
            progress = 0.20f
        )
        
        viewModelScope.launch {
            _events.emit(KycEvent.NavigateToNext)
        }
    }

    // Set the temporary token from account creation response
    fun setTempToken(token: String) {
        _uiState.value = _uiState.value.copy(tempToken = token)
    }

    // Navigation helpers - proper flow without manual entry
    fun goBack() {
        val currentStep = _uiState.value.currentStep
        val newStep = when (currentStep) {
            KycStep.CREATE_ACCOUNT -> KycStep.WELCOME
            KycStep.ADD_PHONE -> KycStep.CREATE_ACCOUNT
            KycStep.VERIFY_PHONE -> KycStep.ADD_PHONE
            else -> currentStep
        }
        
        val newProgress = when (newStep) {
            KycStep.WELCOME -> 0.0f
            KycStep.CREATE_ACCOUNT -> 0.25f
            KycStep.ADD_PHONE -> 0.50f
            KycStep.VERIFY_PHONE -> 0.75f
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
            KycStep.CREATE_ACCOUNT -> "Create Account"
            KycStep.ADD_PHONE -> "Add Phone Number"
            KycStep.VERIFY_PHONE -> "Verify Phone Number"
            KycStep.COMPLETE -> "Registration Complete"
            else -> ""
        }
    }

    fun getStepDescription(): String {
        return when (_uiState.value.currentStep) {
            KycStep.WELCOME -> "Let's get you set up securely in just a few minutes."
            KycStep.CREATE_ACCOUNT -> "Create an account with your details"
            KycStep.ADD_PHONE -> "Enter your phone number for verification"
            KycStep.VERIFY_PHONE -> "Enter the verification code sent to your phone"
            KycStep.COMPLETE -> "Your account has been created successfully"
            else -> ""
        }
    }

    fun getProgressText(): String {
        val step = when (_uiState.value.currentStep) {
            KycStep.WELCOME -> "Welcome"
            KycStep.CREATE_ACCOUNT -> "1 of 4"
            KycStep.ADD_PHONE -> "2 of 4"
            KycStep.VERIFY_PHONE -> "3 of 4"
            KycStep.COMPLETE -> "4 of 4"
            else -> "Welcome"
        }
        return if (_uiState.value.currentStep == KycStep.WELCOME) step else "Step $step"
    }

    fun getProgressPercentage(): String {
        return "${(_uiState.value.progress * 100).toInt()}%"
    }
}