package com.example.g_kash.transactions.data

import com.example.g_kash.data.SessionStorage
import com.example.g_kash.transactions.domain.TransactionRepository

/**
 * Implementation of [TransactionRepository] that handles transaction data operations.
 */
class TransactionRepositoryImpl(
    private val sessionStorage: SessionStorage
) : TransactionRepository {
    
    /**
     * Fetches transactions for a specific account.
     * 
     * @param accountId The ID of the account to fetch transactions for.
     * @return List of transactions for the specified account.
     */
    override suspend fun getTransactions(accountId: String): List<Transaction> {
        // TODO: Replace with actual API call
        // This is a placeholder implementation that returns an empty list
        return emptyList()
    }
}
