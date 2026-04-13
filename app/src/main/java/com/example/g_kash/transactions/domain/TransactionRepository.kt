package com.example.g_kash.transactions.domain

import com.example.g_kash.transactions.data.Transaction
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for handling transaction data operations.
 */
interface TransactionRepository {
    val transactions: StateFlow<List<Transaction>>

    /**
     * Fetches transactions for a specific account.
     * @param accountId The ID of the account to fetch transactions for.
     * @return List of transactions for the specified account.
     */
    suspend fun getTransactions(accountId: String): List<Transaction>

    suspend fun addTransaction(transaction: Transaction)

    suspend fun replaceTransactions(transactions: List<Transaction>)
}
