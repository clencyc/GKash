package com.example.g_kash.transactions.data

import kotlinx.serialization.Serializable
import java.security.Timestamp

@Serializable
data class Transaction(
    val id: Int,
    val accountid: Int, // foreign key to Accounts table
    val amount: Double,
    val type: TransactionType,
    val timestamp: Timestamp,

    )

enum class TransactionType {
    DEPOSIT, WITHDRAWAL, TRANSFER
}
