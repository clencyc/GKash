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
    val name: String = "",
    val phoneNumber: String = "",
    val idNumber: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTermsAccepted: Boolean = false
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
    object NavigateToPin : AuthEvent()
    object NavigateToConfirmPin : AuthEvent()
    object NavigateToApp : AuthEvent()
    data class ShowError(val message: String) : AuthEvent()
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

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updatePhoneNumber(phoneNumber: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phoneNumber)
    }

    fun updateIdNumber(idNumber: String) {
        _uiState.value = _uiState.value.copy(idNumber = idNumber)
    }

    fun toggleTermsAcceptance() {
        _uiState.value = _uiState.value.copy(
            isTermsAccepted = !_uiState.value.isTermsAccepted
        )
    }

    fun createAccount() {
        val state = _uiState.value

        if (!state.isTermsAccepted) {
            _uiState.value = state.copy(error = "Please accept Terms and Conditions")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)

            createAccountUseCase(
                name = state.name,
                phoneNumber = state.phoneNumber,
                idNumber = state.idNumber
            ).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    Log.d("CreateAccount", "User registration response: userId=${response.user_id}, hasTempToken=${response.temp_token.isNotEmpty()}")
                    currentUserId = response.user_id
                    _events.emit(AuthEvent.NavigateToPin)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to create account"
                    )
                }
            )
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
            createPinUseCase(userIdValue, originalPinValue).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    Log.d("CreatePin", "PIN creation response: hasToken=${response.token.isNotEmpty()}")
                    _events.emit(AuthEvent.NavigateToApp)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to create PIN"
                    )
                }
            )
        }
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

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}