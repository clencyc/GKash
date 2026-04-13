package com.example.g_kash.payment.presentation

import com.example.g_kash.accounts.data.Account
import com.example.g_kash.payment.data.PaymentReceipt

// ─────────────────────────────────────────────────────────────
// Workflow sealed interface
// ─────────────────────────────────────────────────────────────

sealed interface PaymentWorkflowState {
    data object Idle : PaymentWorkflowState
    data object Loading : PaymentWorkflowState
    data class AwaitingSTK(
        val transactionId: String,
        val message: String = "Check your phone and approve the M-Pesa prompt."
    ) : PaymentWorkflowState
    data class Success(val receipt: PaymentReceipt) : PaymentWorkflowState
    data class Error(val message: String) : PaymentWorkflowState
}

// ─────────────────────────────────────────────────────────────
// UI state
// ─────────────────────────────────────────────────────────────

data class PaymentUiState(
    val accounts: List<Account> = emptyList(),
    val selectedAccountId: String = "",
    val phone: String = "",
    val amount: String = "",
    val workflowState: PaymentWorkflowState = PaymentWorkflowState.Idle,
    val isLoadingAccounts: Boolean = false,
    val snackbarMessage: String? = null,
    // Keeps track so retry works
    val lastTransactionId: String? = null,
    val showCreateAccountDialog: Boolean = false
) {
    val selectedAccount: Account?
        get() = accounts.find { it.id == selectedAccountId }
}
