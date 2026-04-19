package com.example.g_kash.payment.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────
// REQUEST  – POST /payments/deposit
// ─────────────────────────────────────────────────────────────

@Serializable
data class DepositRequest(
    /** Kenyan phone number e.g. "0712345678" */
    val phone: String,
    val amount: Double,
    @SerialName("account_id")
    val accountId: String
)

// ─────────────────────────────────────────────────────────────
// RESPONSE – POST /payments/deposit
// ─────────────────────────────────────────────────────────────

@Serializable
data class DepositResponse(
    val success: Boolean = false,
    val message: String? = null,
    /** Internal DB transaction id – used to poll status */
    @SerialName("transaction_id")
    val transactionId: String? = null,
    /** External Payhero reference (informational) */
    @SerialName("payhero_reference")
    val payheroReference: String? = null
)

// ─────────────────────────────────────────────────────────────
// RESPONSE – GET /payments/status/{transaction_id}
// ─────────────────────────────────────────────────────────────

@Serializable
data class PaymentStatusResponse(
    /** "SUCCESS", "FAILED", "PENDING", "completed", etc. */
    val status: String? = null,
    /** True when payment is confirmed by M-Pesa */
    val confirmed: Boolean = false,
    /** Backend success flag */
    val success: Boolean = false,
    val amount: Double? = null,
    val timestamp: String? = null,
    /** Alternative timestamp field from some endpoints */
    val date: String? = null,
    @SerialName("transactionReference")
    val transactionReference: String? = null,
    /** Internal transaction id from status body */
    @SerialName("transaction_id")
    val transactionId: String? = null,
    val message: String? = null
)

// ─────────────────────────────────────────────────────────────
// UI models
// ─────────────────────────────────────────────────────────────

data class PaymentReceipt(
    val transactionReference: String,
    val amount: Double,
    val accountId: String,
    val accountType: String,
    val timestamp: String,
    val phone: String,
    val status: String = "Completed"
)
