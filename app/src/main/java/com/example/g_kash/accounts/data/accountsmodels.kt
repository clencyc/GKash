package com.example.g_kash.accounts.data

import kotlinx.serialization.Serializable

/**
 * Account Types supported by the system
 */
enum class AccountType {
    BALANCED_FUND,
    FIXED_INCOME_FUND,
    MONEY_MARKET_FUND,
    STOCK_MARKET
}

/**
 * Account data model
 */
@Serializable
data class Account(
    val accountId: String,
    val accountType: AccountType,
    val userId: String,
    val accountBalance: Double,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Request model for creating a new account
 */
@Serializable
data class CreateAccountRequest(
    val accountType: AccountType,
    val initialBalance: Double = 0.0
)

/**
 * Request model for updating account balance
 */
@Serializable
data class UpdateAccountBalanceRequest(
    val accountId: String,
    val newBalance: Double
)

/**
 * Response model for account operations
 */
@Serializable
data class AccountResponse(
    val success: Boolean,
    val message: String,
    val account: Account? = null
)

/**
 * Response model for listing accounts
 */
@Serializable
data class AccountsListResponse(
    val success: Boolean,
    val accounts: List<Account>
)

/**
 * UI state for account balance display
 */
data class AccountBalanceState(
    val balance: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)