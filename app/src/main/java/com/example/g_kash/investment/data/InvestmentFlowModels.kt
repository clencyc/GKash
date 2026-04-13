package com.example.g_kash.investment.data

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
enum class InvestmentOptionId {
    GKASH_SAVINGS,
    STOCKS,
    BALANCED_FUND,
    MMF
}

data class InvestmentOption(
    val id: InvestmentOptionId,
    val title: String,
    val description: String,
    val comingSoon: Boolean,
    val accentColor: Color
)

data class InvestmentReceipt(
    val transactionReference: String,
    val amount: Double,
    val investmentType: String,
    val timestamp: String,
    val status: String = "Completed"
)

enum class InvestmentStep {
    SELECT_OPTION,
    ENTER_DETAILS
}

sealed interface InvestmentWorkflowState {
    data object Idle : InvestmentWorkflowState
    data object Loading : InvestmentWorkflowState
    data class AwaitingSTK(
        val checkoutRequestId: String,
        val message: String = "STK push sent. Approve the request on your phone."
    ) : InvestmentWorkflowState

    data class Success(val receipt: InvestmentReceipt) : InvestmentWorkflowState
    data class Error(val message: String) : InvestmentWorkflowState
}

data class InvestmentUiState(
    val options: List<InvestmentOption> = defaultInvestmentOptions(),
    val selectedOption: InvestmentOption = defaultInvestmentOptions().first(),
    val step: InvestmentStep = InvestmentStep.SELECT_OPTION,
    val phoneNumber: String = "",
    val amount: String = "",
    val workflowState: InvestmentWorkflowState = InvestmentWorkflowState.Idle,
    val snackbarMessage: String? = null,
    val lastAmount: String = "",
    val lastErrorMessage: String? = null,
    val lastCheckoutRequestId: String? = null
)

fun defaultInvestmentOptions(): List<InvestmentOption> {
    return listOf(
        InvestmentOption(
            id = InvestmentOptionId.GKASH_SAVINGS,
            title = "GKash Savings",
            description = "Grow your money with the active GKash Savings product.",
            comingSoon = false,
            accentColor = Color(0xFF6A5AE0)
        ),
        InvestmentOption(
            id = InvestmentOptionId.STOCKS,
            title = "Stocks",
            description = "Direct market exposure for long-term growth.",
            comingSoon = true,
            accentColor = Color(0xFF9E9E9E)
        ),
        InvestmentOption(
            id = InvestmentOptionId.BALANCED_FUND,
            title = "Balanced Fund",
            description = "A mix of growth and income for moderate risk investors.",
            comingSoon = true,
            accentColor = Color(0xFF9E9E9E)
        ),
        InvestmentOption(
            id = InvestmentOptionId.MMF,
            title = "MMF",
            description = "Liquid, low-risk money market exposure.",
            comingSoon = true,
            accentColor = Color(0xFF9E9E9E)
        )
    )
}
