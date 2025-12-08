package com.example.g_kash.wallet.presentation


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.accounts.data.Account
import com.example.g_kash.wallet.data.WalletRepository
import com.example.g_kash.transactions.data.Transaction
import com.example.g_kash.transactions.data.TransactionType
import com.example.g_kash.transactions.data.TransactionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State holder for the Wallet Screen UI
data class WalletUiState(
    val totalBalance: Double = 1000.0,
    val accounts: List<Account> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class WalletViewModel(
    private val userId: String,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState = _uiState.asStateFlow()

    fun loadWalletData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val accountsResult = walletRepository.getUserAccounts()
            val balanceResult = walletRepository.getTotalBalance()

            // Update state with real data or error
            _uiState.update { currentState ->
                currentState.copy(
                    accounts = accountsResult.getOrElse { emptyList() },
                    totalBalance = balanceResult.getOrElse { 0.0 },
                    recentTransactions = emptyList(),
                    error = listOfNotNull(
                        accountsResult.exceptionOrNull()?.message,
                        balanceResult.exceptionOrNull()?.message
                    ).joinToString("\n").takeIf { it.isNotEmpty() },
                    isLoading = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}