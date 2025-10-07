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
    private val apiService: AccountsApiService
) : AccountsRepository {

    // FIX: Signature now correctly matches the updated interface.
    override suspend fun getUserAccounts(): Flow<Result<List<Account>>> = flow {
        emit(apiService.getUserAccounts())
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
        emit(apiService.getTotalBalance())
    }
}