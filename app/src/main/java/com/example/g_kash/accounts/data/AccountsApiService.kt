package com.example.g_kash.accounts.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * API Service for account-related operations, aligned with API documentation.
 */
class AccountsApiService(
    private val client: HttpClient,
    // FIX: Base URL should probably not include /api here if you add it in every call
    private val baseUrl: String = "https://gkash.onrender.com"
) {

    /**
     * Corresponds to: GET /accounts
     * Fetches all accounts for the authenticated user (identified by auth token).
     */
    suspend fun getUserAccounts(): Result<List<Account>> {
        return try {
            val response = client.get("$baseUrl/accounts") { // FIX: Removed /user/{userId}
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                // Assuming the API returns a JSON object like { "accounts": [...] }
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
     * Corresponds to: GET /accounts/{id}
     * Fetches a specific account by its ID.
     */
    suspend fun getAccountById(accountId: String): Result<Account> {
        return try {
            val response = client.get("$baseUrl/accounts/$accountId") { // FIX: Corrected URL path
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                // Assuming the API returns the Account object directly or wrapped
                // If it's wrapped like { "account": {...} }, use SingleAccountResponse
                // val wrapper: SingleAccountResponse = response.body()
                // Result.success(wrapper.account)

                // If the API returns the Account object directly:
                val account: Account = response.body()
                Result.success(account)
            } else {
                Result.failure(Exception("Failed to fetch account: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Corresponds to: POST /accounts
     * Creates a new account for the authenticated user.
     */
    suspend fun createAccount(request: CreateAccountRequest): Result<Account> {
        return try {
            val response = client.post("$baseUrl/accounts") { // FIX: Removed /user/{userId}
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status == HttpStatusCode.Created) {
                // Assuming the API returns the newly created account object
                val createdAccount: Account = response.body()
                Result.success(createdAccount)
            } else {
                Result.failure(Exception("Failed to create account: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * NOTE: Your API docs don't specify an endpoint for updating a balance.
     * This is a placeholder for a potential: PUT /accounts/{id}/balance
     */
    suspend fun updateAccountBalance(request: UpdateAccountBalanceRequest): Result<Account> {
        return try {
            val response = client.put("$baseUrl/accounts/${request.accountId}/balance") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status == HttpStatusCode.OK) {
                val updatedAccount: Account = response.body()
                Result.success(updatedAccount)
            } else {
                Result.failure(Exception("Failed to update balance: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Corresponds to: DELETE /accounts/{id}
     * Deletes a specific account by its ID.
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
     * NOTE: Your API docs don't specify an endpoint for getting the total balance.
     * This implementation calculates it on the client side, which is a good approach.
     */
    suspend fun getTotalBalance(): Result<Double> {
        return try {
            // FIX: Call the updated getUserAccounts() without userId
            val accountsResult = getUserAccounts()
            accountsResult.map { accounts ->
                accounts.sumOf { it.accountBalance }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}