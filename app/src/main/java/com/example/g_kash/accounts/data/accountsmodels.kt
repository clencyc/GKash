package com.example.g_kash.accounts.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Represents the structure of a single account object from your API
@Serializable
data class Account(
    val id: String,
    @SerialName("account_type")
    val accountType: String,
    @SerialName("balance")
    val accountBalance: Double,
    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String
)

@Serializable
data class TotalBalanceResponse(
    val success: Boolean,
    @SerialName("total_balance")
    val totalBalance: Double,
    val message: String? = null
)


@Serializable
data class AccountsApiResponse( // Renamed for clarity
    val success: Boolean,
    val accounts: List<Account>, // Plural "accounts"
    val message: String? = null
)

// For the GET /accounts endpoint (list of accounts)
@Serializable
data class AccountsListResponse(
    // Assuming your API wraps the list in an "accounts" object
    val accounts: List<Account>
)

// For GET /accounts/{id} and POST /accounts responses
@Serializable
data class SingleAccountResponse(
    // Assuming your API wraps the single object in an "account" object
    val account: Account
)

// For the POST /accounts request body
@Serializable
data class CreateAccountRequest(
    @SerialName("account_type")
    val accountType: String
)

// For updating the balance (assuming this is the structure)
@Serializable
data class UpdateAccountBalanceRequest(
    val accountId: String,
    val amount: Double
)