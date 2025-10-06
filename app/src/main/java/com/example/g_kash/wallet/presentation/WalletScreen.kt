package com.example.g_kash.wallet.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.g_kash.accounts.data.Account
import com.example.g_kash.accounts.presentation.*
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onNavigateToTransactionHistory: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToAccountDetails: (String) -> Unit,
    userId: String
) {
    val viewModel: AccountsViewModel = koinViewModel(
        parameters = { parametersOf(userId) }
    )
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.loadUserAccounts()
        viewModel.loadTotalBalance()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Wallet", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Manage your finances",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Notifications */ }) {
                        Icon(Icons.Default.Notifications, "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Total Balance Card (from your design)
            WalletBalanceCard(
                balance = uiState.totalBalance,
                isLoading = uiState.isLoading,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Actions
            QuickActionsSection(
                onSaveClick = { /* TODO: Save money */ },
                onWithdrawClick = { /* TODO: Withdraw */ },
                onSendClick = { /* TODO: Send money */ },
                onRequestClick = { /* TODO: Request money */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // My Accounts Section
            SectionHeader(
                title = "My Accounts",
                actionText = "View All",
                onActionClick = onNavigateToAccounts
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading && uiState.accounts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.accounts.isEmpty()) {
                EmptyAccountsCard(
                    onCreateAccount = onNavigateToAccounts
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.accounts) { account ->
                        AccountSummaryCard(
                            account = account,
                            onClick = { onNavigateToAccountDetails(account.accountId) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Transactions Section
            SectionHeader(
                title = "Recent Transactions",
                actionText = "See All",
                onActionClick = onNavigateToTransactionHistory
            )

            Spacer(modifier = Modifier.height(12.dp))

            // TODO: Add recent transactions list
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable(onClick = onNavigateToTransactionHistory),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No recent transactions",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Bottom padding
        }
    }

    // Error Snackbar
    uiState.error?.let { error ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Dismiss")
                }
            }
        ) {
            Text(error)
        }
    }
}

@Composable
fun QuickActionsSection(
    onSaveClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onSendClick: () -> Unit,
    onRequestClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                icon = Icons.Default.Savings,
                label = "Save",
                color = Color(0xFFFFC107),
                onClick = onSaveClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.AccountBalance,
                label = "Withdraw",
                color = Color(0xFF2196F3),
                onClick = onWithdrawClick,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                icon = Icons.Default.Send,
                label = "Send",
                color = Color(0xFF4CAF50),
                onClick = onSendClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.CallReceived,
                label = "Request",
                color = Color(0xFFFF5722),
                onClick = onRequestClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(actionText)
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AccountSummaryCard(
    account: Account,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = getAccountTypeColor(account.accountType).copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = getAccountTypeIcon(account.accountType),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = formatAccountType(account.accountType),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatCurrency(account.accountBalance),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun EmptyAccountsCard(
    onCreateAccount: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onCreateAccount),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AccountBalance,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Create Your First Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Start saving and investing today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}