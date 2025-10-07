package com.example.g_kash.accounts.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.accounts.data.*
import com.example.g_kash.accounts.domain.AccountsRepository
import com.google.android.gms.common.internal.AccountType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI State for Accounts Screen
 */
data class AccountsUiState(
    val accounts: List<Account> = emptyList(),
    val totalBalance: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedAccount: Account? = null
)

/**
 * ViewModel for managing accounts
 */
class AccountsViewModel(
    private val repository: AccountsRepository,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountsUiState())
    val uiState: StateFlow<AccountsUiState> = _uiState.asStateFlow()

    init {
        loadUserAccounts()
        loadTotalBalance()
    }

    /**
     * Load all user accounts
     */
    fun loadUserAccounts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.getUserAccounts().collect { result ->
                result.fold(
                    onSuccess = { accounts ->
                        _uiState.update {
                            it.copy(
                                accounts = accounts,
                                isLoading = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to load accounts"
                            )
                        }
                    }
                )
            }
        }
    }

    /**
     * Load total balance across all accounts
     */
    fun loadTotalBalance() {
        viewModelScope.launch {
            repository.getTotalBalance().collect { result ->
                result.fold(
                    onSuccess = { balance ->
                        _uiState.update { it.copy(totalBalance = balance) }
                    },
                    onFailure = { exception ->
                        _uiState.update {
                            it.copy(error = exception.message ?: "Failed to load balance")
                        }
                    }
                )
            }
        }
    }

    /**
     * Create a new account
     */
    fun createAccount(accountType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val request = CreateAccountRequest(accountType = accountType)
            repository.createAccount(request).collect { result ->
                result.fold(
                    onSuccess = { account ->
                        // Reload accounts after creation
                        loadUserAccounts()
                        loadTotalBalance()
                    },
                    onFailure = { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to create account"
                            )
                        }
                    }
                )
            }
        }
    }

    /**
     * Select an account for detailed view
     */
    fun selectAccount(account: Account) {
        _uiState.update { it.copy(selectedAccount = account) }
    }

    /**
     * Deselect current account
     */
    fun deselectAccount() {
        _uiState.update { it.copy(selectedAccount = null) }
    }

    /**
     * Delete an account
     */
    fun deleteAccount(accountId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.deleteAccount(accountId).collect { result ->
                result.fold(
                    onSuccess = {
                        // Reload accounts after deletion
                        loadUserAccounts()
                        loadTotalBalance()
                    },
                    onFailure = { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to delete account"
                            )
                        }
                    }
                )
            }
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}