package com.example.g_kash.accounts.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Represents the structure of a single account object from your API.
// The live API returns:  _id, account_type, account_balance, createdAt, updatedAt
@Serializable
data class Account(
    // The API returns "_id" for createAccount but "id" for list – @SerialName handles the most common one,
    // the HttpClient is configured with ignoreUnknownKeys so missing fields fall back to defaults.
    @SerialName("_id")
    val id: String = "",
    @SerialName("account_type")
    val accountType: String = "",
    @SerialName("account_balance")
    val accountBalance: Double = 0.0,
    @SerialName("createdAt")
    val createdAt: String = "",
    @SerialName("updatedAt")
    val updatedAt: String = ""
)

@Serializable
data class TotalBalanceResponse(
    val success: Boolean = false,
    @SerialName("total_balance")
    val totalBalance: Double = 0.0,
    val message: String? = null
)


@Serializable
data class AccountsApiResponse( // Renamed for clarity
    val success: Boolean = false,
    val accounts: List<Account> = emptyList(), // Plural "accounts"
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

// For GET /accounts/{id}/balance
@Serializable
data class AccountBalanceResponse(
    val success: Boolean = false,
    @SerialName("account_balance")
    val accountBalance: Double = 0.0,
    val message: String? = null
)