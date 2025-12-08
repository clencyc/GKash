package com.example.g_kash.wallet.data

import com.example.g_kash.accounts.data.Account
import com.example.g_kash.authentication.data.ApiService
import com.example.g_kash.transactions.data.Transaction
import com.example.g_kash.transactions.data.TransactionStatus
import com.example.g_kash.transactions.domain.TransactionRepository
import kotlinx.coroutines.flow.Flow

// Interface
interface WalletRepository {
    suspend fun getUserAccounts(): Result<List<Account>>
    suspend fun getTotalBalance(): Result<Double>
    suspend fun getRecentTransactions(limit: Int = 5): Result<List<Transaction>>
}

// Implementation
class WalletRepositoryImpl(
    private val apiService: ApiService,
    private val transactionRepository: TransactionRepository
) : WalletRepository {
    override suspend fun getUserAccounts(): Result<List<Account>> {
        // This might delegate to an existing repository or make its own API call
        return try {
            val response = apiService.getAccounts()
            if (response.success) {
                Result.success(response.accounts)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load accounts"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTotalBalance(): Result<Double> {

        return try {
            val response = apiService.getTotalBalance()
            if (response.success) {
                Result.success(response.totalBalance)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load total balance"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecentTransactions(limit: Int): Result<List<Transaction>> {
        return try {
            val accounts = getUserAccounts().getOrElse { return Result.failure(it) }
            val allTransactions = accounts.flatMap { account ->
                runCatching { 
                    transactionRepository.getTransactions(account.id) 
                }.getOrElse { emptyList() }
            }
            
            Result.success(
                allTransactions
                    .sortedByDescending { it.dateTime }
                    .take(limit)
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}