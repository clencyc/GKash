package com.example.g_kash.transactions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.transactions.data.Transaction
import com.example.g_kash.transactions.domain.TransactionRepository
import com.example.g_kash.wallet.data.BalanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionsUiState(
    val transactions: List<Transaction> = emptyList(),
    val currentBalance: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val accountId: String? = null
)

/**
 * ViewModel for managing transaction data and UI state.
 *
 * @property repository The repository for transaction data operations.
 */
class TransactionsViewModel(
    private val repository: TransactionRepository,
    private val balanceRepository: BalanceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            balanceRepository.balance.collect { balance ->
                _uiState.update { it.copy(currentBalance = balance) }
            }
        }

        viewModelScope.launch {
            repository.transactions.collect { allTransactions ->
                val activeAccountId = _uiState.value.accountId
                val filtered = if (activeAccountId.isNullOrBlank()) {
                    allTransactions
                } else {
                    allTransactions.filter { it.accountId == activeAccountId }
                }

                _uiState.update {
                    it.copy(
                        transactions = filtered,
                        isLoading = false,
                        error = null
                    )
                }
                _transactions.value = filtered
                _isLoading.value = false
                _error.value = null
            }
        }
    }

    /**
     * Loads transactions for the specified account.
     *
     * @param accountId The ID of the account to load transactions for.
     */
    fun loadTransactions(accountId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, accountId = accountId.ifBlank { null }) }
            _isLoading.value = true
            try {
                val loaded = repository.getTransactions(accountId)
                _uiState.update {
                    it.copy(
                        transactions = loaded,
                        isLoading = false,
                        error = null
                    )
                }
                _transactions.value = loaded
                _isLoading.value = false
                _error.value = null
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load transactions"
                    )
                }
                _isLoading.value = false
                _error.value = e.message ?: "Failed to load transactions"
            }
        }
    }

    fun loadAllTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, accountId = null) }
            _isLoading.value = true
            try {
                val loaded = repository.transactions.value
                _uiState.update {
                    it.copy(
                        transactions = loaded,
                        isLoading = false,
                        error = null
                    )
                }
                _transactions.value = loaded
                _isLoading.value = false
                _error.value = null
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load transactions"
                    )
                }
                _isLoading.value = false
                _error.value = e.message ?: "Failed to load transactions"
            }
        }
    }
}
