package com.example.g_kash.wallet.data

import com.example.g_kash.accounts.data.Account
import com.example.g_kash.authentication.data.ApiService
import kotlinx.coroutines.flow.Flow

// Interface
interface WalletRepository {
    suspend fun getUserAccounts(): Result<List<Account>>
    suspend fun getTotalBalance(): Result<Double>
}

// Implementation
class WalletRepositoryImpl(
    private val apiService: ApiService
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
}