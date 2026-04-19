package com.example.g_kash.accounts.domain

import com.example.g_kash.accounts.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*

/**
 * Repository interface for account operations
 */
interface AccountsRepository {
    suspend fun getUserAccounts(): Flow<Result<List<Account>>>
    suspend fun getAccountById(accountId: String): Flow<Result<Account>>
    suspend fun createAccount(request: CreateAccountRequest): Flow<Result<Account>>
    suspend fun updateAccountBalance(request: UpdateAccountBalanceRequest): Flow<Result<Account>>
    suspend fun deleteAccount(accountId: String): Flow<Result<Boolean>>
    suspend fun getTotalBalance(): Flow<Result<Double>>
    suspend fun getAccountBalance(accountId: String): Flow<Result<AccountBalanceResponse>>
    
    // Shared state streams
    val accountsStream: kotlinx.coroutines.flow.StateFlow<List<Account>>
    val totalBalanceStream: kotlinx.coroutines.flow.StateFlow<Double>
    fun refresh()
}

/**
 * Implementation of AccountsRepository
 */
class AccountsRepositoryImpl(
    private val apiService: AccountsApiService
) : AccountsRepository {

    private val repositoryScope = kotlinx.coroutines.MainScope()
    
    private val _accountsStream = kotlinx.coroutines.flow.MutableStateFlow<List<Account>>(emptyList())
    override val accountsStream = _accountsStream.asStateFlow()
    
    private val _totalBalanceStream = kotlinx.coroutines.flow.MutableStateFlow(0.0)
    override val totalBalanceStream = _totalBalanceStream.asStateFlow()

    init {
        // Initial refresh
        refresh()
    }

    override fun refresh() {
        repositoryScope.launch {
            try {
                // 1. Fetch accounts
                val accountsResult = apiService.getUserAccounts()
                accountsResult.onSuccess { accounts ->
                    _accountsStream.value = accounts
                    
                    // 2. Fetch total balance
                    val balanceResult = apiService.getTotalBalance()
                    balanceResult.onSuccess { balance ->
                        _totalBalanceStream.value = balance
                    }.onFailure {
                        // Fallback: calculate total balance from individual accounts if endpoint fails
                        _totalBalanceStream.value = accounts.sumOf { it.accountBalance }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AccountsRepo", "Error refreshing accounts", e)
            }
        }
    }

    override suspend fun getUserAccounts(): Flow<Result<List<Account>>> = flow {
        val result = apiService.getUserAccounts()
        result.onSuccess { 
            _accountsStream.value = it 
            refresh() // Trigger full sync
        }
        emit(result)
    }

    override suspend fun getAccountById(accountId: String): Flow<Result<Account>> = flow {
        emit(apiService.getAccountById(accountId))
    }

    override suspend fun createAccount(
        request: CreateAccountRequest
    ): Flow<Result<Account>> = flow {
        val result = apiService.createAccount(request)
        result.onSuccess { refresh() }
        emit(result)
    }

    override suspend fun updateAccountBalance(
        request: UpdateAccountBalanceRequest
    ): Flow<Result<Account>> = flow {
        val result = apiService.updateAccountBalance(request)
        result.onSuccess { refresh() }
        emit(result)
    }

    override suspend fun deleteAccount(accountId: String): Flow<Result<Boolean>> = flow {
        val result = apiService.deleteAccount(accountId)
        result.onSuccess { refresh() }
        emit(result)
    }

    override suspend fun getTotalBalance(): Flow<Result<Double>> = flow {
        val result = apiService.getTotalBalance()
        result.onSuccess { _totalBalanceStream.value = it }
        emit(result)
    }

    override suspend fun getAccountBalance(accountId: String): Flow<Result<AccountBalanceResponse>> = flow {
        emit(apiService.getAccountBalance(accountId))
    }
}