package com.example.g_kash.authentication.presentation

import com.example.g_kash.authentication.domain.AuthRepository

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.authentication.data.AuthState
import com.example.g_kash.authentication.domain.CreateAccountUseCase
import com.example.g_kash.authentication.domain.CreatePinUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

// Create Account ViewModel
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
                    if (response.success && response.userId != null) {
                        currentUserId = response.userId
                        _events.emit(AuthEvent.NavigateToPin)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = response.message
                        )
                    }
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

// Create Pin ViewModel
class CreatePinViewModel(
    private val createPinUseCase: CreatePinUseCase
) : ViewModel() {

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

// Confirm Pin ViewModel
class ConfirmPinViewModel(
    private val createPinUseCase: CreatePinUseCase
) : ViewModel() {

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
                    if (response.success) {
                        _events.emit(AuthEvent.NavigateToApp)
                    } else {
                        _uiState.value = _uiState.value.copy(error = response.message)
                    }
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

// Main Auth ViewModel
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // --- CORRECTED authState ---
    private val _authState = MutableStateFlow<AuthState>(AuthState(isAuthenticated = false, user = null))
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    init {
        // Load initial auth state when the ViewModel is created
        loadAuthState()
    }

    private fun loadAuthState() {
        viewModelScope.launch {

            authRepository.getAuthState().collect { repoState ->
                _authState.value = repoState // Update _authState when repository emits
            }

            // OPTION 2: If authRepository.getAuthState() is just logic to CHECK a session
            // (e.g., returns Boolean and fetches user on success), you'd do this:
            /*
            val user = authRepository.fetchUserSession() // Example function
            if (user != null) {
                _authState.value = AuthState(isAuthenticated = true, user = user)
            } else {
                _authState.value = AuthState(isAuthenticated = false, user = null)
            }
            */
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.clearAuthData() // This should ideally update the authState in the repository's flow
            // If clearing auth data doesn't automatically update the flow, you might need:
            // _authState.value = AuthState(isAuthenticated = false, user = null)
        }
    }
}