package com.example.g_kash.transactions.data

import com.example.g_kash.data.SessionStorage
import com.example.g_kash.transactions.domain.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Implementation of [TransactionRepository] that handles transaction data operations.
 */
class TransactionRepositoryImpl(
    private val sessionStorage: SessionStorage
) : TransactionRepository {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    override val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()
    
    /**
     * Fetches transactions for a specific account.
     * 
     * @param accountId The ID of the account to fetch transactions for.
     * @return List of transactions for the specified account.
     */
    override suspend fun getTransactions(accountId: String): List<Transaction> {
        val allTransactions = transactions.value
        return if (accountId.isBlank()) {
            allTransactions
        } else {
            allTransactions.filter { it.accountId == accountId }
        }
    }

    override suspend fun addTransaction(transaction: Transaction) {
        val resolvedUserId = if (transaction.accountId.isBlank()) {
            sessionStorage.getCurrentUserId() ?: "investment"
        } else {
            transaction.accountId
        }

        val resolvedTransaction = transaction.copy(accountId = resolvedUserId)
        _transactions.update { current ->
            listOf(resolvedTransaction) + current
        }
    }

    override suspend fun replaceTransactions(transactions: List<Transaction>) {
        _transactions.value = transactions.sortedByDescending { it.dateTime }
    }
}
