package com.example.g_kash.accounts.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
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

@Composable
fun getAccountTypeColor(type: AccountType): Color {
    val isDark = isSystemInDarkTheme()
    return when (type) {
        // Balanced Fund is now our signature Pink
        AccountType.BALANCED_FUND -> if (isDark) Color(0xFFC11C84) else Color(0xFFE91E63)
        // Fixed Income Fund is a professional Blue
        AccountType.FIXED_INCOME_FUND -> if (isDark) Color(0xFF42A5F5) else Color(0xFF1976D2)
        // Money Market Fund is a premium Gold/Amber
        AccountType.MONEY_MARKET_FUND -> if (isDark) Color(0xFFFFB300) else Color(0xFFEFBF04)
        // Stock Market is a bold Orange/Red
        AccountType.STOCK_MARKET -> if (isDark) Color(0xFFFF7043) else Color(0xFFFF5722)
    }
}
