package com.example.g_kash.wallet.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g_kash.accounts.presentation.formatCurrency
import com.example.g_kash.transactions.data.Transaction
import com.example.g_kash.transactions.data.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WalletTransactionCard(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Initials Circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getInitialsBackgroundColor(transaction.type)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transaction.reference ?: "--",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Transaction Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatTransactionSubtitle(transaction),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            // Amount and Time
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatTransactionAmount(transaction),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = getAmountColor(transaction.type)
                )
                Text(
                    text = formatTime(transaction.dateTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

private fun getInitialsBackgroundColor(type: TransactionType): Color {
    return when (type) {
        TransactionType.WITHDRAWAL -> Color(0xFF4CAF50) // Green
        TransactionType.INVESTMENT -> Color(0xFF2196F3) // Blue  
        TransactionType.INTEREST -> Color(0xFFFF9800) // Orange
        else -> Color(0xFF9C27B0) // Purple
    }
}

private fun formatTransactionAmount(transaction: Transaction): String {
    val prefix = when (transaction.type) {
        TransactionType.DEPOSIT, TransactionType.TRANSFER_IN, TransactionType.INTEREST -> "+"
        TransactionType.WITHDRAWAL, TransactionType.TRANSFER_OUT, TransactionType.INVESTMENT -> "-"
    }
    return "$prefix KSH ${formatCurrency(transaction.amount).replace("KES ", "")}"
}

private fun getAmountColor(type: TransactionType): Color {
    return when (type) {
        TransactionType.DEPOSIT, TransactionType.TRANSFER_IN, TransactionType.INTEREST -> Color(0xFF4CAF50) // Green
        TransactionType.WITHDRAWAL, TransactionType.TRANSFER_OUT, TransactionType.INVESTMENT -> Color(0xFFFF5722) // Red
    }
}

private fun formatTime(dateTime: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(dateTime)
        date?.let { outputFormat.format(it) } ?: dateTime
    } catch (e: Exception) {
        dateTime
    }
}

private fun formatTransactionSubtitle(transaction: Transaction): String {
    return when (transaction.type) {
        TransactionType.WITHDRAWAL -> "Sent • M-Pesa"
        TransactionType.INVESTMENT -> "Investment Purchase"
        TransactionType.INTEREST -> "Interest Earned"
        else -> "Transaction"
    }
}