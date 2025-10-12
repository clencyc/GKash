package com.example.g_kash.accounts.domain

import com.example.g_kash.accounts.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository interface for account operations
 */
interface AccountsRepository {
    // FIX: Removed userId. The user is identified by the auth token now.
    suspend fun getUserAccounts(): Flow<Result<List<Account>>>
    suspend fun getAccountById(accountId: String): Flow<Result<Account>>
    // FIX: Removed userId.
    suspend fun createAccount(request: CreateAccountRequest): Flow<Result<Account>>
    suspend fun updateAccountBalance(request: UpdateAccountBalanceRequest): Flow<Result<Account>>
    suspend fun deleteAccount(accountId: String): Flow<Result<Boolean>>
    // FIX: Removed userId.
    suspend fun getTotalBalance(): Flow<Result<Double>>
}

/**
 * Implementation of AccountsRepository
 */
class AccountsRepositoryImpl(
    private val apiService: AccountsApiService,
    private val demoMode: Boolean = true // Enable demo mode with dummy data
) : AccountsRepository {

    // FIX: Signature now correctly matches the updated interface.
    override suspend fun getUserAccounts(): Flow<Result<List<Account>>> = flow {
        if (demoMode) {
            // Dummy Kenyan accounts with reasonable KES amounts
            val dummyAccounts = listOf(
                Account(
                    id = "acc_001",
                    accountType = "balanced_fund",
                    accountBalance = 125000.0, // KES 125,000
                    createdAt = "2024-01-15T10:30:00Z",
                    updatedAt = "2024-03-10T14:20:00Z"
                ),
                Account(
                    id = "acc_002",
                    accountType = "money_market_fund",
                    accountBalance = 85750.0, // KES 85,750
                    createdAt = "2024-02-20T09:15:00Z",
                    updatedAt = "2024-03-11T11:45:00Z"
                ),
                Account(
                    id = "acc_003",
                    accountType = "fixed_income_fund",
                    accountBalance = 250000.0, // KES 250,000
                    createdAt = "2024-01-05T16:00:00Z",
                    updatedAt = "2024-03-09T08:30:00Z"
                ),
                Account(
                    id = "acc_004",
                    accountType = "stock_market",
                    accountBalance = 47500.0, // KES 47,500
                    createdAt = "2024-03-01T12:00:00Z",
                    updatedAt = "2024-03-12T10:15:00Z"
                )
            )
            emit(Result.success(dummyAccounts))
        } else {
            emit(apiService.getUserAccounts())
        }
    }

    override suspend fun getAccountById(accountId: String): Flow<Result<Account>> = flow {
        emit(apiService.getAccountById(accountId))
    }

    // FIX: Signature now matches the interface, syntax error is fixed, and call is correct.
    override suspend fun createAccount(
        request: CreateAccountRequest
    ): Flow<Result<Account>> = flow {
        emit(apiService.createAccount(request))
    }

    override suspend fun updateAccountBalance(
        request: UpdateAccountBalanceRequest
    ): Flow<Result<Account>> = flow {
        emit(apiService.updateAccountBalance(request))
    }

    override suspend fun deleteAccount(accountId: String): Flow<Result<Boolean>> = flow {
        emit(apiService.deleteAccount(accountId))
    }

    // FIX: Signature now correctly matches the updated interface.
    override suspend fun getTotalBalance(): Flow<Result<Double>> = flow {
        if (demoMode) {
            // Calculate total from dummy accounts: 125,000 + 85,750 + 250,000 + 47,500 = 508,250
            val totalBalance = 508250.0
            emit(Result.success(totalBalance))
        } else {
            emit(apiService.getTotalBalance())
        }
    }
}