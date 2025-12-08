package com.example.g_kash.transactions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.transactions.data.Transaction
import com.example.g_kash.transactions.domain.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing transaction data and UI state.
 *
 * @property repository The repository for transaction data operations.
 */
class TransactionsViewModel(
    private val repository: TransactionRepository
) : ViewModel() {
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    /**
     * Loads transactions for the specified account.
     *
     * @param accountId The ID of the account to load transactions for.
     */
    fun loadTransactions(accountId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _transactions.value = repository.getTransactions(accountId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load transactions"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
