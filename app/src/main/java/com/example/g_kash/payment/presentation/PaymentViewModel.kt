package com.example.g_kash.payment.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.accounts.data.Account
import com.example.g_kash.accounts.domain.AccountsRepository
import com.example.g_kash.payment.data.DepositRequest
import com.example.g_kash.payment.data.PaymentReceipt
import com.example.g_kash.payment.data.PaymentRepository
import com.example.g_kash.wallet.data.BalanceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "PaymentViewModel"

class PaymentViewModel(
    private val paymentRepository: PaymentRepository,
    private val accountsRepository: AccountsRepository,
    private val balanceRepository: BalanceRepository,
    /** Pre-selected account id when entry is from AccountDetails */
    private val preselectedAccountId: String = ""
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState(selectedAccountId = preselectedAccountId))
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    fun loadAccounts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAccounts = true) }
            try {
                accountsRepository.getUserAccounts()
                    .collect { result ->
                        result.fold(
                            onSuccess = { accounts ->
                                val firstId = if (preselectedAccountId.isNotBlank()) {
                                    preselectedAccountId
                                } else {
                                    accounts.firstOrNull()?.id ?: ""
                                }
                                _uiState.update {
                                    it.copy(
                                        accounts = accounts,
                                        selectedAccountId = it.selectedAccountId.ifBlank { firstId },
                                        isLoadingAccounts = false
                                    )
                                }
                            },
                            onFailure = { e ->
                                _uiState.update { it.copy(isLoadingAccounts = false) }
                                Log.e(TAG, "Failed to load accounts", e)
                            }
                        )
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingAccounts = false) }
                Log.e(TAG, "Exception loading accounts", e)
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // UI event handlers
    // ─────────────────────────────────────────────────────────

    fun selectAccount(account: Account) {
        _uiState.update { it.copy(selectedAccountId = account.id) }
    }

    fun onPhoneChanged(phone: String) {
        _uiState.update { it.copy(phone = phone, workflowState = PaymentWorkflowState.Idle) }
    }

    fun onAmountChanged(amount: String) {
        _uiState.update { it.copy(amount = amount, workflowState = PaymentWorkflowState.Idle) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun consumeSuccess() {
        _uiState.update {
            if (it.workflowState is PaymentWorkflowState.Success) {
                it.copy(workflowState = PaymentWorkflowState.Idle, amount = "", phone = "")
            } else it
        }
    }

    fun submitDeposit() {
        val state = _uiState.value

        val amount = state.amount.trim().toDoubleOrNull()
        val phone = state.phone.trim()
        val accountId = state.selectedAccountId

        if (amount == null || amount <= 0.0) {
            _uiState.update { it.copy(workflowState = PaymentWorkflowState.Error("Please enter a valid amount.")) }
            return
        }
        if (phone.isBlank() || phone.length < 10) {
            _uiState.update { it.copy(workflowState = PaymentWorkflowState.Error("Please enter a valid phone number (e.g. 0712345678).")) }
            return
        }
        if (accountId.isBlank()) {
            _uiState.update { it.copy(workflowState = PaymentWorkflowState.Error("Please select an account first.")) }
            return
        }

        _uiState.update { it.copy(workflowState = PaymentWorkflowState.Loading) }

        viewModelScope.launch {
            val request = DepositRequest(phone = phone, amount = amount, accountId = accountId)
            paymentRepository.initiateDeposit(request).fold(
                onSuccess = { depositResponse ->
                    val txId = depositResponse.transactionId
                    if (txId.isNullOrBlank()) {
                        handleFailure("Deposit initiated but no transaction ID returned.")
                        return@fold
                    }

                    _uiState.update {
                        it.copy(
                            workflowState = PaymentWorkflowState.AwaitingSTK(txId),
                            lastTransactionId = txId
                        )
                    }

                    // Poll until confirmed
                    awaitPaymentConfirmation(txId, amount).fold(
                        onSuccess = { statusResponse ->
                            Log.d(TAG, "Polling Success: status=${statusResponse.status}, confirmed=${statusResponse.confirmed}")
                            val confirmed = statusResponse.confirmed || 
                                statusResponse.status.equals("SUCCESS", ignoreCase = true) ||
                                statusResponse.status.equals("completed", ignoreCase = true)

                            if (!confirmed) {
                                handleFailure("Payment is still pending. Please try again shortly.")
                                return@fold
                            }

                            val accountType = state.selectedAccount?.accountType ?: "Account"
                            val reference = statusResponse.transactionReference
                                ?: statusResponse.transactionId
                                ?: txId.takeLast(10).uppercase(Locale.getDefault())
                            
                            val timestamp = statusResponse.timestamp 
                                ?: statusResponse.date 
                                ?: nowTimestamp()

                            val receipt = PaymentReceipt(
                                transactionReference = reference,
                                amount = amount,
                                accountId = accountId,
                                accountType = accountType,
                                timestamp = timestamp,
                                phone = phone
                            )
                            
                            // Update shared balance repository
                            balanceRepository.adjustBalance(amount)
                            android.util.Log.i(TAG, "Balance adjusted by +$amount")

                            _uiState.update { it.copy(workflowState = PaymentWorkflowState.Success(receipt)) }
                        },
                        onFailure = { e ->
                            handleFailure(e.message ?: "Payment confirmation failed.")
                        }
                    )
                },
                onFailure = { e ->
                    handleFailure(e.message ?: "Failed to send STK push. Please try again.")
                }
            )
        }
    }

    private suspend fun awaitPaymentConfirmation(
        transactionId: String,
        amount: Double
    ): Result<com.example.g_kash.payment.data.PaymentStatusResponse> {
        return withTimeoutOrNull(60_000L) {
            // Poll up to 8 times, every 7 seconds = up to ~56 seconds
            repeat(8) { attempt ->
                val result = paymentRepository.getTransactionStatus(transactionId)
                if (result.isSuccess) {
                    val status = result.getOrThrow()
                    if (status.confirmed || 
                        status.status.equals("SUCCESS", ignoreCase = true) ||
                        status.status.equals("completed", ignoreCase = true)) {
                        Log.i(TAG, "✓ Payment confirmed: $transactionId")
                        return@withTimeoutOrNull Result.success(status)
                    }
                    Log.d(TAG, "Payment still pending (attempt ${attempt + 1}): status=${status.status}")
                    if (status.status.equals("FAILED", ignoreCase = true) || 
                        status.status.equals("CANCELLED", ignoreCase = true)) {
                        return@withTimeoutOrNull Result.failure(
                            Exception(status.message ?: "Payment was cancelled or failed.")
                        )
                    }
                } else if (attempt == 7) {
                    return@withTimeoutOrNull Result.failure(
                        result.exceptionOrNull() ?: Exception("Payment confirmation failed")
                    )
                }
                delay(7_000L)
            }
            Result.failure(Exception("Payment confirmation timed out"))
        } ?: Result.failure(Exception("Payment timed out. Check your M-Pesa messages."))
    }

    // ─────────────────────────────────────────────────────────
    // Account Creation
    // ─────────────────────────────────────────────────────────

    fun setShowCreateAccountDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateAccountDialog = show) }
    }

    fun createAccount(accountType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAccounts = true) }
            val request = com.example.g_kash.accounts.data.CreateAccountRequest(
                accountType = accountType
            )
            accountsRepository.createAccount(request).collect { result ->
                result.fold(
                    onSuccess = {
                        // Refresh accounts list
                        loadAccounts()
                        _uiState.update { it.copy(showCreateAccountDialog = false) }
                    },
                    onFailure = { e ->
                        _uiState.update { 
                            it.copy(
                                isLoadingAccounts = false,
                                snackbarMessage = "Failed to create account: ${e.message}"
                            ) 
                        }
                    }
                )
            }
        }
    }

    private fun handleFailure(message: String) {
        Log.w(TAG, "Payment failed: $message")
        _uiState.update {
            it.copy(
                workflowState = PaymentWorkflowState.Error(message),
                snackbarMessage = message
            )
        }
    }

    private fun nowTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
}
