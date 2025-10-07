package com.example.g_kash.accounts.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.NumberFormat
import java.util.Locale


// All your helper functions now live in this one file.
fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "KE"))
    formatter.maximumFractionDigits = 2
    return formatter.format(amount)
}

fun formatAccountType(type: AccountType): String {
    return type.displayName
}

fun getAccountTypeIcon(type: AccountType): ImageVector {
    return when (type) {
        AccountType.BALANCED_FUND -> Icons.Default.AccountBalance
        AccountType.FIXED_INCOME_FUND -> Icons.Default.Lock
        AccountType.MONEY_MARKET_FUND -> Icons.Default.Savings
        AccountType.STOCK_MARKET -> Icons.Default.TrendingUp
    }
}

fun getAccountTypeColor(type: AccountType): Color {
    return when (type) {
        AccountType.BALANCED_FUND -> Color(0xFF4CAF50)
        AccountType.FIXED_INCOME_FUND -> Color(0xFF2196F3)
        AccountType.MONEY_MARKET_FUND -> Color(0xFFFFC107)
        AccountType.STOCK_MARKET -> Color(0xFFFF5722)
    }
}
