package com.example.g_kash.accounts.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * API Service for account-related operations
 */
class AccountsApiService(
    private val client: HttpClient,
    private val baseUrl: String = "https://api.gkash.com" // Replace with actual API URL
) {

    /**
     * Fetch all accounts for the authenticated user
     */
    suspend fun getUserAccounts(userId: String): Result<List<Account>> {
        return try {
            val response = client.get("$baseUrl/accounts/user/$userId") {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val accountsResponse: AccountsListResponse = response.body()
                Result.success(accountsResponse.accounts)
            } else {
                Result.failure(Exception("Failed to fetch accounts: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch a specific account by ID
     */
    suspend fun getAccountById(accountId: String): Result<Account> {
        return try {
            val response = client.get("$baseUrl/accounts/$accountId") {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val accountResponse: AccountResponse = response.body()
                accountResponse.account?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Account not found"))
            } else {
                Result.failure(Exception("Failed to fetch account: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a new account
     */
    suspend fun createAccount(
        userId: String,
        request: CreateAccountRequest
    ): Result<Account> {
        return try {
            val response = client.post("$baseUrl/accounts/user/$userId") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status == HttpStatusCode.Created) {
                val accountResponse: AccountResponse = response.body()
                accountResponse.account?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Failed to create account"))
            } else {
                Result.failure(Exception("Failed to create account: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update account balance
     */
    suspend fun updateAccountBalance(request: UpdateAccountBalanceRequest): Result<Account> {
        return try {
            val response = client.put("$baseUrl/accounts/${request.accountId}/balance") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status == HttpStatusCode.OK) {
                val accountResponse: AccountResponse = response.body()
                accountResponse.account?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Failed to update balance"))
            } else {
                Result.failure(Exception("Failed to update balance: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete an account
     */
    suspend fun deleteAccount(accountId: String): Result<Boolean> {
        return try {
            val response = client.delete("$baseUrl/accounts/$accountId") {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete account: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get total balance across all accounts
     */
    suspend fun getTotalBalance(userId: String): Result<Double> {
        return try {
            val accountsResult = getUserAccounts(userId)
            accountsResult.map { accounts ->
                accounts.sumOf { it.accountBalance }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}