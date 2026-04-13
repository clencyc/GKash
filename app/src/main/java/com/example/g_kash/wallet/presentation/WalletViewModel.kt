package com.example.g_kash.wallet.presentation


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.accounts.data.Account
import com.example.g_kash.wallet.data.WalletRepository
import com.example.g_kash.wallet.data.BalanceRepository
import com.example.g_kash.transactions.data.Transaction
import com.example.g_kash.transactions.domain.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

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
    private val walletRepository: WalletRepository,
    private val balanceRepository: BalanceRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            balanceRepository.balance.collect { balance ->
                _uiState.update { it.copy(totalBalance = balance) }
            }
        }

        viewModelScope.launch {
            transactionRepository.transactions.collect { transactions ->
                _uiState.update { currentState ->
                    currentState.copy(
                        recentTransactions = transactions
                            .filter { userId.isBlank() || it.accountId == userId || it.accountId == "investment" }
                            .sortedByDescending { it.dateTime }
                            .take(5)
                    )
                }
            }
        }
    }

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