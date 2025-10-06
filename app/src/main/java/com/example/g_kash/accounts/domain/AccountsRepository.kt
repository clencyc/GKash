package com.example.g_kash.accounts.domain

import com.example.g_kash.accounts.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository interface for account operations
 */
interface AccountsRepository {
    suspend fun getUserAccounts(userId: String): Flow<Result<List<Account>>>
    suspend fun getAccountById(accountId: String): Flow<Result<Account>>
    suspend fun createAccount(userId: String, request: CreateAccountRequest): Flow<Result<Account>>
    suspend fun updateAccountBalance(request: UpdateAccountBalanceRequest): Flow<Result<Account>>
    suspend fun deleteAccount(accountId: String): Flow<Result<Boolean>>
    suspend fun getTotalBalance(userId: String): Flow<Result<Double>>
}

/**
 * Implementation of AccountsRepository
 */
class AccountsRepositoryImpl(
    private val apiService: AccountsApiService
) : AccountsRepository {

    override suspend fun getUserAccounts(userId: String): Flow<Result<List<Account>>> = flow {
        emit(apiService.getUserAccounts(userId))
    }

    override suspend fun getAccountById(accountId: String): Flow<Result<Account>> = flow {
        emit(apiService.getAccountById(accountId))
    }

    override suspend fun createAccount(
        userId: String,
        request: CreateAccountRequest
    ): Flow<Result<Account>> = flow {
        emit(apiService.createAccount(userId, request))
    }

    override suspend fun updateAccountBalance(
        request: UpdateAccountBalanceRequest
    ): Flow<Result<Account>> = flow {
        emit(apiService.updateAccountBalance(request))
    }

    override suspend fun deleteAccount(accountId: String): Flow<Result<Boolean>> = flow {
        emit(apiService.deleteAccount(accountId))
    }

    override suspend fun getTotalBalance(userId: String): Flow<Result<Double>> = flow {
        emit(apiService.getTotalBalance(userId))
    }
}