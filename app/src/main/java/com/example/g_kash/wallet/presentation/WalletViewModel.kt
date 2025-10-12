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
            
            // Generate dummy transactions
            val dummyTransactions = generateDummyTransactions()

            accountsResult.onSuccess { accounts ->
                _uiState.update { it.copy(accounts = accounts) }
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
            }

            balanceResult.onSuccess { balance ->
                _uiState.update { it.copy(totalBalance = balance, recentTransactions = dummyTransactions) }
            }.onFailure { error ->
                // Use dummy data on failure for demo
                _uiState.update { it.copy(totalBalance = 1000.0, recentTransactions = dummyTransactions, error = null) }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    private fun generateDummyTransactions(): List<Transaction> {
        return listOf(
            Transaction(
                transactionId = "TXN001",
                accountId = "acc_001",
                type = TransactionType.WITHDRAWAL,
                amount = 200.0,
                status = TransactionStatus.COMPLETED,
                description = "GKash Stock",
                dateTime = "2025-10-11 07:45:00",
                reference = "MN"
            ),
            Transaction(
                transactionId = "TXN002",
                accountId = "acc_001",
                type = TransactionType.INVESTMENT,
                amount = 3000.0,
                status = TransactionStatus.COMPLETED,
                description = "Investment",
                dateTime = "2025-08-13 10:00:00",
                reference = "IN"
            ),
            Transaction(
                transactionId = "TXN003",
                accountId = "acc_001",
                type = TransactionType.INTEREST,
                amount = 187.0,
                status = TransactionStatus.COMPLETED,
                description = "Interest Paid",
                dateTime = "2025-08-12 02:30:00",
                reference = "IP"
            )
        )
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}