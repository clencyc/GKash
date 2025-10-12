package com.example.g_kash.investment.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

/**
 * Investment account types with their characteristics
 */
@Serializable
enum class InvestmentAccountType(
    val displayName: String,
    val description: String,
    val riskLevel: RiskLevel,
    val minimumAmount: Double,
    val expectedReturn: String,
    val features: List<String>
) {
    MONEY_MARKET_FUND(
        displayName = "Money Market Fund",
        description = "Low-risk investment with steady returns. Perfect for beginners and capital preservation.",
        riskLevel = RiskLevel.LOW,
        minimumAmount = 1000.0,
        expectedReturn = "8-12% annually",
        features = listOf(
            "High liquidity - withdraw anytime",
            "Capital preservation focused",
            "Regulated by CMA",
            "Daily pricing"
        )
    ),
    BALANCED_FUND(
        displayName = "Balanced Fund",
        description = "Mix of stocks and bonds for moderate risk and growth potential.",
        riskLevel = RiskLevel.MEDIUM,
        minimumAmount = 5000.0,
        expectedReturn = "12-18% annually",
        features = listOf(
            "Diversified portfolio",
            "Professional fund management",
            "Quarterly dividends",
            "Medium-term growth focus"
        )
    ),
    EQUITY_FUND(
        displayName = "Equity Fund",
        description = "Stock market investments for higher growth potential with higher risk.",
        riskLevel = RiskLevel.HIGH,
        minimumAmount = 10000.0,
        expectedReturn = "15-25% annually",
        features = listOf(
            "Higher growth potential",
            "NSE listed companies",
            "Dividend income",
            "Long-term wealth building"
        )
    ),
    BOND_FUND(
        displayName = "Fixed Income Fund",
        description = "Government and corporate bonds for stable income generation.",
        riskLevel = RiskLevel.LOW,
        minimumAmount = 2500.0,
        expectedReturn = "10-15% annually",
        features = listOf(
            "Fixed income streams",
            "Government backed securities",
            "Predictable returns",
            "Lower volatility"
        )
    )
}

@Serializable
enum class RiskLevel(
    val displayName: String,
    val color: Long, // Using Long for Color serialization
    val description: String
) {
    LOW("Low Risk", 0xFF4CAF50, "Conservative approach with capital protection"),
    MEDIUM("Medium Risk", 0xFFFF9800, "Balanced approach with moderate growth"),
    HIGH("High Risk", 0xFFF44336, "Aggressive approach with high growth potential")
}

/**
 * Investment account creation request
 */
@Serializable
data class CreateInvestmentAccountRequest(
    val accountType: String, // Uses the enum name
    val initialDeposit: Double,
    val riskTolerance: String,
    val investmentGoal: String,
    val investmentHorizon: String
)

/**
 * Investment goal options
 */
@Serializable
enum class InvestmentGoal(
    val displayName: String,
    val description: String
) {
    WEALTH_BUILDING("Wealth Building", "Long-term wealth accumulation"),
    RETIREMENT("Retirement Planning", "Building retirement nest egg"),
    EDUCATION("Education Fund", "Saving for education expenses"),
    HOME_PURCHASE("Home Purchase", "Saving for property investment"),
    EMERGENCY_FUND("Emergency Fund", "Building emergency reserves"),
    PASSIVE_INCOME("Passive Income", "Generate regular income")
}

/**
 * Investment horizon (time period) options
 */
@Serializable
enum class InvestmentHorizon(
    val displayName: String,
    val description: String,
    val minMonths: Int,
    val maxMonths: Int?
) {
    SHORT_TERM("Short Term", "1-2 years", 12, 24),
    MEDIUM_TERM("Medium Term", "3-5 years", 36, 60),
    LONG_TERM("Long Term", "5+ years", 60, null)
}

/**
 * Risk tolerance assessment
 */
@Serializable
enum class RiskTolerance(
    val displayName: String,
    val description: String
) {
    CONSERVATIVE("Conservative", "I prefer stable returns with minimal risk"),
    MODERATE("Moderate", "I can accept some risk for better returns"),
    AGGRESSIVE("Aggressive", "I'm comfortable with high risk for potentially high returns")
}

/**
 * UI state for investment account creation
 */
data class InvestmentAccountCreationUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedAccountType: InvestmentAccountType? = null,
    val selectedGoal: InvestmentGoal? = null,
    val selectedHorizon: InvestmentHorizon? = null,
    val selectedRiskTolerance: RiskTolerance? = null,
    val initialDeposit: Double = 0.0,
    val currentStep: InvestmentCreationStep = InvestmentCreationStep.SELECT_TYPE,
    val isAccountCreated: Boolean = false
)

/**
 * Steps in the investment account creation flow
 */
enum class InvestmentCreationStep {
    SELECT_TYPE,
    SET_GOALS,
    RISK_ASSESSMENT,
    INITIAL_DEPOSIT,
    REVIEW_AND_CONFIRM
}

/**
 * Helper extensions for UI
 */
fun InvestmentAccountType.getIcon(): ImageVector {
    return when (this) {
        InvestmentAccountType.MONEY_MARKET_FUND -> Icons.Default.AccountBalance
        InvestmentAccountType.BALANCED_FUND -> Icons.Default.BarChart
        InvestmentAccountType.EQUITY_FUND -> Icons.Default.TrendingUp
        InvestmentAccountType.BOND_FUND -> Icons.Default.Security
    }
}

fun RiskLevel.getColor(): Color {
    return Color(this.color)
}

fun InvestmentGoal.getIcon(): ImageVector {
    return when (this) {
        InvestmentGoal.WEALTH_BUILDING -> Icons.Default.MonetizationOn
        InvestmentGoal.RETIREMENT -> Icons.Default.Person
        InvestmentGoal.EDUCATION -> Icons.Default.School
        InvestmentGoal.HOME_PURCHASE -> Icons.Default.Home
        InvestmentGoal.EMERGENCY_FUND -> Icons.Default.Security
        InvestmentGoal.PASSIVE_INCOME -> Icons.Default.Payments
    }
}
