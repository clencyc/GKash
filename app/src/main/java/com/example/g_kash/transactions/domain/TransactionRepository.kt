package com.example.g_kash.transactions.domain

import com.example.g_kash.transactions.data.Transaction

/**
 * Repository interface for handling transaction data operations.
 */
interface TransactionRepository {
    /**
     * Fetches transactions for a specific account.
     * @param accountId The ID of the account to fetch transactions for.
     * @return List of transactions for the specified account.
     */
    suspend fun getTransactions(accountId: String): List<Transaction>
}
