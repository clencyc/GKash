package com.example.g_kash.wallet.presentation

// ... your other imports
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import com.example.g_kash.accounts.presentation.AccountSummaryCard
import com.example.g_kash.accounts.presentation.SectionHeader
import com.example.g_kash.accounts.presentation.WalletBalanceCard
import com.example.g_kash.wallet.presentation.WalletTransactionCard
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    // These navigation callbacks are perfect. Do not change them.
    onNavigateToTransactionHistory: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToAccountDetails: (accountId: String) -> Unit,
    onNavigateToInvestment: () -> Unit = {},
    userId: String
) {
    // --- THIS IS THE FIX ---
    // We inject the NEW WalletViewModel, passing the userId it needs.
    // There is NO MORE AccountsViewModel here.
    val viewModel: WalletViewModel = koinViewModel(
        parameters = { parametersOf(userId) }
    )
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Load data when the screen is first composed
    LaunchedEffect(Unit) {
        viewModel.loadWalletData()
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "Welcome",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Muteria",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notification click */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = { /* Search click */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Wallet Balance Card with Invest button
            WalletBalanceCard(
                balance = uiState.totalBalance,
                isLoading = uiState.isLoading,
                onInvestClick = onNavigateToInvestment,
                onWithdrawClick = { /* Handle withdraw */ }
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
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.accounts.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No accounts yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create your first account to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(uiState.accounts) { account ->
                        AccountSummaryCard(
                            account = account,
                            onClick = { onNavigateToAccountDetails(account.id) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Transactions Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onNavigateToTransactionHistory) {
                    Text("View All")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Date header
            Text(
                text = "11 Oct, 07:45 AM",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // Recent Transactions List
            if (uiState.recentTransactions.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "No transactions yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    uiState.recentTransactions.forEach { transaction ->
                        WalletTransactionCard(
                            transaction = transaction,
                            onClick = { /* Handle transaction click */ }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp)) // Bottom padding for navigation
        }
    }
}

