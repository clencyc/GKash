package com.example.g_kash.authentication.presentation

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.authentication.domain.AuthRepository
import com.example.g_kash.authentication.domain.CreateAccountUseCase
import com.example.g_kash.authentication.domain.CreatePinUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// UI States
data class CreateAccountUiState(
    val fullName: String = "",
    val email: String = "",
    val pin: String = "",
    val confirmPin: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTermsAccepted: Boolean = false
)

data class PhoneVerificationUiState(
    val phoneNumber: String = "",
    val otpCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOtpSent: Boolean = false,
    val isPhoneVerified: Boolean = false
)

data class PinUiState(
    val pin: String = "",
    val confirmPin: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPinComplete: Boolean = false
)

// Events
sealed class AuthEvent {
    object NavigateToPhoneVerification : AuthEvent()
    object NavigateToPin : AuthEvent()
    object NavigateToConfirmPin : AuthEvent()
    object NavigateToApp : AuthEvent()
    data class ShowError(val message: String) : AuthEvent()
    data class ShowSuccess(val message: String) : AuthEvent()
}

// --- THIS IS THE NEW UI STATE DEFINITION ---
/**
 * Represents the possible authentication states for the UI.
 */
sealed class UiAuthState {
    object Unknown : UiAuthState()        // Initial state, we are checking for a token
    object Authenticated : UiAuthState()   // A token exists, user is logged in
    object Unauthenticated : UiAuthState() // No token, user is logged out
}


// Create Account ViewModel (This remains unchanged)
class CreateAccountViewModel(
    private val createAccountUseCase: CreateAccountUseCase
) : ViewModel() {

    private val _uiState = mutableStateOf(CreateAccountUiState())
    val uiState: State<CreateAccountUiState> = _uiState

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    private var currentUserId: String? = null

    fun updateFullName(name: String) {
        _uiState.value = _uiState.value.copy(fullName = name)
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun updatePin(pin: String) {
        if (pin.length <= 4 && pin.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(pin = pin)
        }
    }

    fun updateConfirmPin(confirmPin: String) {
        if (confirmPin.length <= 4 && confirmPin.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(confirmPin = confirmPin)
        }
    }

    fun toggleTermsAcceptance() {
        _uiState.value = _uiState.value.copy(
            isTermsAccepted = !_uiState.value.isTermsAccepted
        )
    }

    fun createAccount() {
        val state = _uiState.value

        // Validation
        if (state.fullName.isBlank()) {
            _uiState.value = state.copy(error = "Please enter your full name")
            return
        }
        if (state.email.isBlank()) {
            _uiState.value = state.copy(error = "Please enter your email")
            return
        }
        if (!state.email.contains("@")) {
            _uiState.value = state.copy(error = "Please enter a valid email")
            return
        }
        if (state.pin.length != 4) {
            _uiState.value = state.copy(error = "PIN must be 4 digits")
            return
        }
        if (!state.isTermsAccepted) {
            _uiState.value = state.copy(error = "Please accept Terms and Conditions")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)

            val result = createAccountUseCase(
                name = state.fullName,
                email = state.email,
                pin = state.pin,
                confirmPin = state.confirmPin
            )
            result.onSuccess { response ->
                _uiState.value = _uiState.value.copy(isLoading = false)
                if (response.success && response.user != null && response.token != null) {
                    Log.d("CreateAccount", "User registration response: userId=${response.user.id}, hasToken=${response.token.isNotEmpty()}")
                    currentUserId = response.user.id
                    _events.emit(AuthEvent.NavigateToPhoneVerification)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = response.message
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to create account"
                )
            }
        }
    }

    fun getCurrentUserId(): String? = currentUserId
}

// Create Pin ViewModel (This remains unchanged)
class CreatePinViewModel(
    private val createPinUseCase: CreatePinUseCase
) : ViewModel() {
    // ... your existing code ...
    private val _uiState = mutableStateOf(PinUiState())
    val uiState: State<PinUiState> = _uiState
    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()
    private var userId: String? = null
    fun setUserId(id: String) {
        userId = id
    }
    fun updatePin(pin: String) {
        if (pin.length <= 4) {
            _uiState.value = _uiState.value.copy(
                pin = pin,
                isPinComplete = pin.length == 4
            )
        }
    }
    fun proceedToConfirmPin() {
        val state = _uiState.value
        if (state.pin.length == 4) {
            viewModelScope.launch {
                _events.emit(AuthEvent.NavigateToConfirmPin)
            }
        }
    }
    fun getPin(): String = _uiState.value.pin
}

// Confirm Pin ViewModel (This remains unchanged)
class ConfirmPinViewModel(
    private val createPinUseCase: CreatePinUseCase
) : ViewModel() {
    // ... your existing code ...
    private val _uiState = mutableStateOf(PinUiState())
    val uiState: State<PinUiState> = _uiState
    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()
    private var userId: String? = null
    private var originalPin: String? = null
    fun setUserIdAndPin(id: String, pin: String) {
        userId = id
        originalPin = pin
    }
    fun updateConfirmPin(pin: String) {
        if (pin.length <= 4) {
            _uiState.value = _uiState.value.copy(
                confirmPin = pin,
                isPinComplete = pin.length == 4
            )
        }
    }
    fun confirmPin() {
        val state = _uiState.value
        val userIdValue = userId
        val originalPinValue = originalPin
        if (userIdValue == null || originalPinValue == null) {
            _uiState.value = state.copy(error = "Invalid session")
            return
        }
        if (state.confirmPin != originalPinValue) {
            _uiState.value = state.copy(error = "PINs do not match")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val result = createPinUseCase(originalPinValue)
            result.onSuccess { response ->
                _uiState.value = _uiState.value.copy(isLoading = false)
                if (response.success && response.token != null) {
                    Log.d("CreatePin", "PIN creation response: hasToken=${response.token.isNotEmpty()}")
                    _events.emit(AuthEvent.NavigateToApp)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = response.message
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to create PIN"
                )
            }
        }
    }
}

// Phone Verification ViewModel
class PhoneVerificationViewModel(
    private val sendOtpUseCase: com.example.g_kash.otp.domain.SendOtpUseCase,
    private val verifyOtpUseCase: com.example.g_kash.otp.domain.VerifyOtpUseCase
) : ViewModel() {
    private val _uiState = mutableStateOf(PhoneVerificationUiState())
    val uiState: State<PhoneVerificationUiState> = _uiState
    
    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()
    
    private var userName: String? = null
    
    fun setUserName(name: String) {
        userName = name
    }
    
    fun updatePhoneNumber(phoneNumber: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phoneNumber)
    }
    
    fun updateOtpCode(code: String) {
        if (code.length <= 6 && code.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(otpCode = code)
        }
    }
    
    fun sendOtp() {
        val state = _uiState.value
        val name = userName
        
        if (state.phoneNumber.isBlank()) {
            _uiState.value = state.copy(error = "Please enter your phone number")
            return
        }
        
        if (name.isNullOrBlank()) {
            _uiState.value = state.copy(error = "User name not found. Please restart registration.")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            
            val result = sendOtpUseCase(state.phoneNumber, name)
            result.onSuccess { response ->
                if (response.success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isOtpSent = true
                    )
                    _events.emit(AuthEvent.ShowSuccess("OTP sent to ${state.phoneNumber}"))
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.message
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to send OTP"
                )
            }
        }
    }
    
    fun verifyOtp() {
        val state = _uiState.value
        
        if (state.otpCode.length != 6) {
            _uiState.value = state.copy(error = "Please enter the 6-digit OTP")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            
            val result = verifyOtpUseCase(state.phoneNumber, state.otpCode)
            result.onSuccess { response ->
                if (response.success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isPhoneVerified = true
                    )
                    _events.emit(AuthEvent.ShowSuccess("Phone number verified successfully!"))
                    _events.emit(AuthEvent.NavigateToApp)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.message
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to verify OTP"
                )
            }
        }
    }
    
    fun resendOtp() {
        _uiState.value = _uiState.value.copy(otpCode = "", isOtpSent = false)
        sendOtp()
    }
}


// --- THE FINAL, CORRECTED AuthViewModel ---
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    init {
        // Debug logging on startup
        viewModelScope.launch {
            try {
                val token = authRepository.getAuthTokenStream().first()
                Log.d("AuthViewModel", "=== AUTH STARTUP DEBUG ===")
                Log.d("AuthViewModel", "Token on startup: ${if (token != null) "Present (${token.take(10)}...)" else "NULL"}")
                Log.d("AuthViewModel", "============================")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error checking startup auth state", e)
            }
        }
    }

    // No more _authState or loadAuthState()!

    /**
     * This is the single source of truth for the UI's authentication state.
     */
    val uiAuthState: StateFlow<UiAuthState> = authRepository.getAuthTokenStream()
        .map { token ->
            if (token != null) {
                UiAuthState.Authenticated
            } else {
                UiAuthState.Unauthenticated
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiAuthState.Unknown
        )

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(email: String, pin: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val result = authRepository.login(email, pin)
                if (result.isSuccess) {
                    val response = result.getOrNull()
                    if (response?.success == true && response.token != null) {
                        _loginState.value = LoginState.Success
                    } else {
                        _loginState.value = LoginState.Error(response?.message ?: "Invalid credentials")
                    }
                } else {
                    _loginState.value = LoginState.Error(result.exceptionOrNull()?.message ?: "Login failed")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}