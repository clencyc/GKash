package com.example.g_kash.investment.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.analytics.AnalyticsHelper
import com.example.g_kash.investment.data.InvestmentOption
import com.example.g_kash.investment.data.InvestmentOptionId
import com.example.g_kash.investment.data.InvestmentReceipt
import com.example.g_kash.investment.data.InvestmentStep
import com.example.g_kash.investment.data.InvestmentRepository
import com.example.g_kash.investment.data.InvestmentUiState
import com.example.g_kash.investment.data.InvestmentWorkflowState
import com.example.g_kash.transactions.data.Transaction
import com.example.g_kash.transactions.data.TransactionStatus
import com.example.g_kash.transactions.data.TransactionType
import com.example.g_kash.transactions.domain.TransactionRepository
import com.example.g_kash.wallet.data.BalanceRepository
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InvestmentViewModel(
    private val investmentRepository: InvestmentRepository,
    private val balanceRepository: BalanceRepository,
    private val transactionRepository: TransactionRepository,
    private val firebaseAnalytics: FirebaseAnalytics
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvestmentUiState())
    val uiState: StateFlow<InvestmentUiState> = _uiState.asStateFlow()

    fun selectOption(option: InvestmentOption) {
        if (option.comingSoon) {
            _uiState.update { it.copy(snackbarMessage = "${option.title} is coming soon.") }
            return
        }

        _uiState.update {
            it.copy(
                selectedOption = option,
                step = InvestmentStep.SELECT_OPTION,
                workflowState = InvestmentWorkflowState.Idle,
                snackbarMessage = null,
                lastErrorMessage = null
            )
        }
    }

    fun goToDetailsStep() {
        _uiState.update { it.copy(step = InvestmentStep.ENTER_DETAILS, snackbarMessage = null) }
    }

    fun goBackToOptionsStep() {
        _uiState.update { it.copy(step = InvestmentStep.SELECT_OPTION, snackbarMessage = null) }
    }

    fun onPhoneChanged(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone, snackbarMessage = null, lastErrorMessage = null) }
    }

    fun onAmountChanged(amount: String) {
        _uiState.update { it.copy(amount = amount, snackbarMessage = null, lastErrorMessage = null) }
    }

    fun submitInvestment() {
        val state = _uiState.value
        val selectedOption = state.selectedOption
        val amount = state.amount.trim().toDoubleOrNull()
        val phone = state.phoneNumber.trim()

        if (selectedOption.id != InvestmentOptionId.GKASH_SAVINGS) {
            _uiState.update {
                it.copy(snackbarMessage = "Only GKash Savings is available today.")
            }
            return
        }

        if (amount == null || amount <= 0.0) {
            _uiState.update {
                it.copy(workflowState = InvestmentWorkflowState.Error("Enter a valid investment amount."))
            }
            return
        }

        if (phone.isBlank() || phone.length < 10) {
            _uiState.update {
                it.copy(workflowState = InvestmentWorkflowState.Error("Enter a valid phone number."))
            }
            return
        }

        _uiState.update {
            it.copy(
                workflowState = InvestmentWorkflowState.Loading,
                lastAmount = state.amount,
                lastErrorMessage = null,
                snackbarMessage = null
            )
        }
        viewModelScope.launch {
            AnalyticsHelper.logEvent(
                eventName = AnalyticsHelper.Events.INVESTMENT_INITIATED,
                params = mapOf("amount" to amount, "type" to selectedOption.title),
                firebaseAnalytics = firebaseAnalytics
            )

            val stkPushResult = investmentRepository.initiateStkPush(amount, selectedOption.title, phone)
            stkPushResult.fold(
                onSuccess = { response ->
                    val checkoutRequestId = response.checkoutRequestId
                        ?: response.checkoutRequestIdFallback
                        ?: "checkout-${System.currentTimeMillis()}"

                    AnalyticsHelper.logEvent(
                        eventName = AnalyticsHelper.Events.STK_PUSH_SENT,
                        params = mapOf("amount" to amount, "type" to selectedOption.title),
                        firebaseAnalytics = firebaseAnalytics
                    )

                    _uiState.update {
                        it.copy(
                            workflowState = InvestmentWorkflowState.AwaitingSTK(checkoutRequestId),
                            lastCheckoutRequestId = checkoutRequestId
                        )
                    }

                    val confirmation = awaitConfirmation(checkoutRequestId)
                    confirmation.fold(
                        onSuccess = { statusResponse ->
                            if (!statusResponse.confirmed && !statusResponse.status.equals("SUCCESS", ignoreCase = true)) {
                                handleFailure("Payment is still pending. Try again shortly.", "PENDING")
                                return@fold
                            }

                            val transactionReference = statusResponse.transactionReference
                                ?: checkoutRequestId.takeLast(10).uppercase(Locale.getDefault())
                            val timestamp = statusResponse.timestamp ?: nowTimestamp()
                            val receipt = InvestmentReceipt(
                                transactionReference = transactionReference,
                                amount = amount,
                                investmentType = selectedOption.title,
                                timestamp = timestamp
                            )

                            balanceRepository.adjustBalance(-amount)
                            transactionRepository.addTransaction(
                                Transaction(
                                    transactionId = transactionReference,
                                    accountId = "",
                                    type = TransactionType.INVESTMENT,
                                    amount = amount,
                                    status = TransactionStatus.COMPLETED,
                                    description = "GKash Savings",
                                    dateTime = timestamp,
                                    reference = transactionReference
                                )
                            )

                            AnalyticsHelper.logEvent(
                                eventName = AnalyticsHelper.Events.INVESTMENT_SUCCESS,
                                params = mapOf(
                                    "amount" to amount,
                                    "type" to selectedOption.title,
                                    "transaction_ref" to transactionReference
                                ),
                                firebaseAnalytics = firebaseAnalytics
                            )

                            _uiState.update {
                                it.copy(
                                    workflowState = InvestmentWorkflowState.Success(receipt),
                                    snackbarMessage = null,
                                    lastErrorMessage = null
                                )
                            }
                        },
                        onFailure = { error ->
                            handleFailure(error.message ?: "Investment confirmation failed.", error.javaClass.simpleName)
                        }
                    )
                },
                onFailure = { error ->
                    handleFailure(error.message ?: "Unable to send STK push.", error.javaClass.simpleName)
                }
            )
        }
    }

    private suspend fun awaitConfirmation(checkoutRequestId: String) = kotlinx.coroutines.withTimeoutOrNull(45000L) {
        repeat(6) { attempt ->
            val statusResult = investmentRepository.pollInvestmentStatus(checkoutRequestId)
            if (statusResult.isSuccess) {
                val statusResponse = statusResult.getOrThrow()
                if (statusResponse.confirmed || statusResponse.status.equals("SUCCESS", ignoreCase = true)) {
                    return@withTimeoutOrNull Result.success(statusResponse)
                }
            } else if (attempt == 5) {
                return@withTimeoutOrNull Result.failure(statusResult.exceptionOrNull() ?: Exception("Investment confirmation failed"))
            }
            delay(5000L)
        }
        Result.failure(Exception("Investment confirmation timed out"))
    } ?: Result.failure(Exception("Investment confirmation timed out"))

    private fun handleFailure(message: String, errorCode: String) {
        Log.w("InvestmentViewModel", "Investment failed: $message ($errorCode)")
        AnalyticsHelper.logEvent(
            eventName = AnalyticsHelper.Events.INVESTMENT_FAILED,
            params = mapOf("error_code" to errorCode),
            firebaseAnalytics = firebaseAnalytics
        )
        _uiState.update {
            it.copy(
                workflowState = InvestmentWorkflowState.Error(message),
                snackbarMessage = message,
                lastErrorMessage = message
            )
        }
    }

    fun retryLastInvestment() {
        val message = _uiState.value.lastErrorMessage ?: "Retrying investment"
        _uiState.update { it.copy(snackbarMessage = message, workflowState = InvestmentWorkflowState.Idle) }
        submitInvestment()
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun consumeSuccessState() {
        _uiState.update { state ->
            if (state.workflowState is InvestmentWorkflowState.Success) {
                state.copy(workflowState = InvestmentWorkflowState.Idle, step = InvestmentStep.SELECT_OPTION)
            } else {
                state
            }
        }
    }

    private fun nowTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }
}
