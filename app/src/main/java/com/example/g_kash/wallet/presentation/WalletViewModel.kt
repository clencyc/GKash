package com.example.g_kash.wallet.presentation


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.accounts.data.Account
import com.example.g_kash.wallet.data.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State holder for the Wallet Screen UI
data class WalletUiState(
    val totalBalance: Double = 0.0,
    val accounts: List<Account> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class WalletViewModel(
    private val userId: String, // Keep this if your API calls need it
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState = _uiState.asStateFlow()

    fun loadWalletData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val accountsResult = walletRepository.getUserAccounts()
            val balanceResult = walletRepository.getTotalBalance()

            accountsResult.onSuccess { accounts ->
                _uiState.update { it.copy(accounts = accounts) }
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
            }

            balanceResult.onSuccess { balance ->
                _uiState.update { it.copy(totalBalance = balance) }
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}