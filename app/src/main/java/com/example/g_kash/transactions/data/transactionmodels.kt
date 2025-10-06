package com.example.g_kash.transactions.data

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.g_kash.accounts.presentation.formatCurrency
import java.text.SimpleDateFormat
import java.util.*

// Mock Transaction Models (these should come from your transaction module)
enum class TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER_IN,
    TRANSFER_OUT,
    INVESTMENT,
    INTEREST
}

enum class TransactionStatus {
    COMPLETED,
    PENDING,
    FAILED
}

data class Transaction(
    val transactionId: String,
    val accountId: String,
    val type: TransactionType,
    val amount: Double,
    val status: TransactionStatus,
    val description: String,
    val dateTime: String,
    val reference: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountTransactionsScreen(
    accountId: String,
    onNavigateBack: () -> Unit,
    onTransactionClick: (String) -> Unit
) {
    // TODO: Replace with actual ViewModel
    val transactions = remember { generateMockTransactions(accountId) }
    var selectedFilter by remember { mutableStateOf<TransactionType?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }

    val filteredTransactions = if (selectedFilter != null) {
        transactions.filter { it.type == selectedFilter }
    } else {
        transactions
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Transactions") },
                            onClick = {
                                selectedFilter = null
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                if (selectedFilter == null) {
                                    Icon(Icons.Default.Check, null)
                                }
                            }
                        )
                        Divider()
                        TransactionType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(formatTransactionType(type)) },
                                onClick = {
                                    selectedFilter = type
                                    showFilterMenu = false
                                },
                                leadingIcon = {
                                    if (selectedFilter == type) {
                                        Icon(Icons.Default.Check, null)
                                    }
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = transactions.size.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = transactions.count { it.type == TransactionType.DEPOSIT }.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        Text(
                            text = "Deposits",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = transactions.count { it.type == TransactionType.WITHDRAWAL }.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5722)
                        )
                        Text(
                            text = "Withdrawals",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Active Filter Chip
            if (selectedFilter != null) {
                FilterChip(
                    selected = true,
                    onClick = { selectedFilter = null },
                    label = { Text(formatTransactionType(selectedFilter!!)) },
                    trailingIcon = { Icon(Icons.Default.Close, "Clear", Modifier.size(18.dp)) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Transactions List
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No transactions found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTransactions) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            onClick = { onTransactionClick(transaction.transactionId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCard(
    transaction: Transaction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transaction Icon
            Surface(
                shape = CircleShape,
                color = getTransactionTypeColor(transaction.type).copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = getTransactionTypeIcon(transaction.type),
                        contentDescription = null,
                        tint = getTransactionTypeColor(transaction.type),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Transaction Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDateTime(transaction.dateTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusChip(status = transaction.status)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatTransactionAmount(transaction.type, transaction.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = getAmountColor(transaction.type)
                )
                transaction.reference?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: TransactionStatus) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = getStatusColor(status).copy(alpha = 0.1f),
        modifier = Modifier.clip(RoundedCornerShape(4.dp))
    ) {
        Text(
            text = status.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = getStatusColor(status),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// Helper Functions
fun formatTransactionType(type: TransactionType): String {
    return when (type) {
        TransactionType.DEPOSIT -> "Deposits"
        TransactionType.WITHDRAWAL -> "Withdrawals"
        TransactionType.TRANSFER_IN -> "Transfers In"
        TransactionType.TRANSFER_OUT -> "Transfers Out"
        TransactionType.INVESTMENT -> "Investments"
        TransactionType.INTEREST -> "Interest"
    }
}

fun getTransactionTypeIcon(type: TransactionType) = when (type) {
    TransactionType.DEPOSIT -> Icons.Default.ArrowDownward
    TransactionType.WITHDRAWAL -> Icons.Default.ArrowUpward
    TransactionType.TRANSFER_IN -> Icons.Default.CallReceived
    TransactionType.TRANSFER_OUT -> Icons.Default.CallMade
    TransactionType.INVESTMENT -> Icons.Default.TrendingUp
    TransactionType.INTEREST -> Icons.Default.Paid
}

fun getTransactionTypeColor(type: TransactionType) = when (type) {
    TransactionType.DEPOSIT -> Color(0xFF4CAF50)
    TransactionType.WITHDRAWAL -> Color(0xFFFF5722)
    TransactionType.TRANSFER_IN -> Color(0xFF2196F3)
    TransactionType.TRANSFER_OUT -> Color(0xFFFF9800)
    TransactionType.INVESTMENT -> Color(0xFF9C27B0)
    TransactionType.INTEREST -> Color(0xFF009688)
}

fun getStatusColor(status: TransactionStatus) = when (status) {
    TransactionStatus.COMPLETED -> Color(0xFF4CAF50)
    TransactionStatus.PENDING -> Color(0xFFFF9800)
    TransactionStatus.FAILED -> Color(0xFFF44336)
}

fun getAmountColor(type: TransactionType) = when (type) {
    TransactionType.DEPOSIT, TransactionType.TRANSFER_IN, TransactionType.INTEREST -> Color(0xFF4CAF50)
    TransactionType.WITHDRAWAL, TransactionType.TRANSFER_OUT, TransactionType.INVESTMENT -> Color(0xFFFF5722)
}

fun formatTransactionAmount(type: TransactionType, amount: Double): String {
    val prefix = when (type) {
        TransactionType.DEPOSIT, TransactionType.TRANSFER_IN, TransactionType.INTEREST -> "+"
        TransactionType.WITHDRAWAL, TransactionType.TRANSFER_OUT, TransactionType.INVESTMENT -> "-"
    }
    return "$prefix ${formatCurrency(amount)}"
}

fun formatDateTime(dateTime: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(dateTime)
        date?.let { outputFormat.format(it) } ?: dateTime
    } catch (e: Exception) {
        dateTime
    }
}

// Mock data generator (remove in production)
fun generateMockTransactions(accountId: String): List<Transaction> {
    return listOf(
        Transaction(
            transactionId = "TXN001",
            accountId = accountId,
            type = TransactionType.DEPOSIT,
            amount = 5000.0,
            status = TransactionStatus.COMPLETED,
            description = "Deposit from M-Pesa",
            dateTime = "2025-10-05 09:30:00",
            reference = "MPE001"
        ),
        Transaction(
            transactionId = "TXN002",
            accountId = accountId,
            type = TransactionType.WITHDRAWAL,
            amount = 1000.0,
            status = TransactionStatus.COMPLETED,
            description = "ATM Withdrawal",
            dateTime = "2025-10-04 14:15:00",
            reference = "ATM789"
        ),
        Transaction(
            transactionId = "TXN003",
            accountId = accountId,
            type = TransactionType.INVESTMENT,
            amount = 3000.0,
            status = TransactionStatus.COMPLETED,
            description = "Investment Purchase",
            dateTime = "2025-10-03 11:20:00",
            reference = "INV456"
        ),
        Transaction(
            transactionId = "TXN004",
            accountId = accountId,
            type = TransactionType.INTEREST,
            amount = 150.0,
            status = TransactionStatus.COMPLETED,
            description = "Monthly Interest",
            dateTime = "2025-10-01 00:00:00",
            reference = "INT789"
        ),
        Transaction(
            transactionId = "TXN005",
            accountId = accountId,
            type = TransactionType.TRANSFER_IN,
            amount = 2000.0,
            status = TransactionStatus.PENDING,
            description = "Transfer from John Doe",
            dateTime = "2025-09-30 16:45:00",
            reference = "TRF123"
        ),
        Transaction(
            transactionId = "TXN006",
            accountId = accountId,
            type = TransactionType.TRANSFER_OUT,
            amount = 500.0,
            status = TransactionStatus.FAILED,
            description = "Transfer to Jane Smith",
            dateTime = "2025-09-29 10:30:00",
            reference = "TRF124"
        )
    )
}