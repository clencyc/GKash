package com.example.g_kash.wallet.presentation


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.accounts.data.Account
import com.example.g_kash.wallet.data.WalletRepository
import com.example.g_kash.wallet.data.BalanceRepository
import com.example.g_kash.transactions.data.Transaction
import com.example.g_kash.transactions.domain.TransactionRepository
import com.example.g_kash.accounts.domain.AccountsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// State holder for the Wallet Screen UI
data class WalletUiState(
    val totalBalance: Double = 0.0,
    val accounts: List<Account> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class WalletViewModel(
    private val userId: String,
    private val walletRepository: WalletRepository,
    private val balanceRepository: BalanceRepository,
    private val transactionRepository: TransactionRepository,
    private val accountsRepository: AccountsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Collect shared balance directly from repository
        viewModelScope.launch {
            accountsRepository.totalBalanceStream.collect { balance ->
                _uiState.update { it.copy(totalBalance = balance) }
            }
        }

        // Collect shared accounts directly from repository
        viewModelScope.launch {
            accountsRepository.accountsStream.collect { accounts ->
                _uiState.update { it.copy(accounts = accounts) }
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
            android.util.Log.d("WalletViewModel", "loadWalletData starting... User: $userId")
            
            // Trigger background refresh in repository
            accountsRepository.refresh()
            
            // Artificial delay to show loading state if needed, or simply let collection handle it
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}