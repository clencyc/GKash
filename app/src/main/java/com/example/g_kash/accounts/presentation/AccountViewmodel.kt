package com.example.g_kash.accounts.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.accounts.data.Account
import com.example.g_kash.accounts.data.CreateAccountRequest
import com.example.g_kash.accounts.domain.AccountsRepository
// FIX 1: Removed the incorrect import for com.google.android.gms.common.internal.AccountType
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
    private val accountsRepository: AccountsRepository // This is the correct property name
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountsUiState())
    val uiState: StateFlow<AccountsUiState> = _uiState.asStateFlow()

    init {
        // Load initial data when the ViewModel is created
        loadUserAccounts()
        loadTotalBalance()
    }

    /**
     * Load all user accounts
     */
    fun loadUserAccounts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // FIX 2 & 4: Use the correct repository name and the recommended flow collection
            accountsRepository.getUserAccounts()
                .onEach { result ->
                    result.fold(
                        onSuccess = { accounts ->
                            _uiState.update {
                                it.copy(
                                    accounts = accounts,
                                    isLoading = false
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
                .launchIn(viewModelScope) // Use launchIn for safer collection
        }
    }

    /**
     * Load total balance across all accounts
     */
    fun loadTotalBalance() {
        viewModelScope.launch {
            // FIX 3: Use the correct repository property name 'accountsRepository'
            accountsRepository.getTotalBalance()
                .onEach { result ->
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
                .launchIn(viewModelScope)
        }
    }

    /**
     * Create a new account
     */
    fun createAccount(accountType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val request = CreateAccountRequest(accountType = accountType)
            // FIX 3: Use the correct repository property name 'accountsRepository'
            accountsRepository.createAccount(request)
                .onEach { result ->
                    result.fold(
                        onSuccess = {
                            // Reload data after successful creation
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
                .launchIn(viewModelScope)
        }
    }

    /**
     * Delete an account
     */
    fun deleteAccount(accountId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // FIX 3: Use the correct repository property name 'accountsRepository'
            accountsRepository.deleteAccount(accountId)
                .onEach { result ->
                    result.fold(
                        onSuccess = {
                            // Reload data after successful deletion
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
                .launchIn(viewModelScope)
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
     * Clear error state
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
