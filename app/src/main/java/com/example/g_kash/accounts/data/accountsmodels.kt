package com.example.g_kash.accounts.data

import kotlinx.serialization.Serializable

@Serializable
data class Accounts (
    val accountid: Int,
    val accounttype: String,
    val userid: Int,
    val accountbalance: Double,
    val createdat: String,
    val updatedat: String
)