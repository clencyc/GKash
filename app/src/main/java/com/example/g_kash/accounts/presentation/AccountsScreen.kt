package com.example.g_kash.accounts.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.g_kash.accounts.data.Account
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.*

// --- FIX 1: DEFINE THE MISSING AccountType ENUM ---
// This enum holds display information and the raw string value the API expects.
enum class AccountType(val displayName: String, val apiValue: String) {
    BALANCED_FUND("Balanced Fund", "balanced fund"),
    FIXED_INCOME_FUND("Fixed Income Fund", "bond fund"),
    MONEY_MARKET_FUND("Money Market Fund", "money market fund"),
    STOCK_MARKET("Stock Market", "equity fund")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel = koinViewModel(),
    onNavigateToTransactions: (String) -> Unit,
    onNavigateToDeposit: (String) -> Unit, // NEW
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    // Auto-refresh when screen appears
    LaunchedEffect(Unit) {
        viewModel.loadUserAccounts()
        viewModel.loadTotalBalance()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Accounts", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Add Account")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Total Balance Card
            WalletBalanceCard(
                balance = uiState.totalBalance,
                isLoading = uiState.isLoading,
                onInvestClick = { onNavigateToDeposit("") } // General invest
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Accounts List Header
            Text(
                text = "Your Accounts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error Display
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Close, "Dismiss")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Loading or Empty State
            when {
                uiState.isLoading && uiState.accounts.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.accounts.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No accounts yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Create your first account to get started", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
                else -> {
                    // Accounts List
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.accounts) { account ->
                            AccountCard(
                                account = account,
                                onClick = {
                                    onNavigateToTransactions(account.id)
                                },
                                onInvestClick = {
                                    onNavigateToDeposit(account.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Create Account Dialog
    if (showCreateDialog) {
        CreateAccountDialog(
            onDismiss = { showCreateDialog = false },
            // --- FIX 3: UPDATE THE `onCreate` LAMBDA SIGNATURE ---
            onCreate = { accountApiValue ->
                // The ViewModel expects only the API string value
                viewModel.createAccount(accountApiValue)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun AccountCard(
    account: Account, 
    onClick: () -> Unit,
    onInvestClick: () -> Unit // NEW
) {
    // This helper function converts the raw string from the API back to our enum
    val accountTypeEnum = AccountType.values().find { it.apiValue == account.accountType } ?: AccountType.BALANCED_FUND

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = getAccountTypeColor(accountTypeEnum).copy(alpha = 0.1f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = getAccountTypeIcon(accountTypeEnum),
                            contentDescription = null,
                            tint = getAccountTypeColor(accountTypeEnum),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatAccountType(accountTypeEnum),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Active Account",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Current Balance",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = formatCurrency(account.accountBalance),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                Button(
                    onClick = onInvestClick,
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Invest", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CreateAccountDialog(
    onDismiss: () -> Unit,
    // --- FIX 3 (continued): The dialog now provides the raw API string ---
    onCreate: (apiValue: String) -> Unit
) {
    var selectedType by remember { mutableStateOf<AccountType?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Account") },
        text = {
            Column {
                Text("Select Account Type:")
                Spacer(modifier = Modifier.height(8.dp))
                // Iterate through the enum we defined at the top of the file
                AccountType.values().forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = type }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { selectedType = type }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(formatAccountType(type))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedType?.let { type ->
                        // Pass the raw API string value ("balanced_fund", etc.)
                        onCreate(type.apiValue)
                    }
                },
                enabled = selectedType != null
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// WalletBalanceCard does not need changes
@Composable
fun WalletBalanceCard(
    balance: Double, 
    isLoading: Boolean,
    onInvestClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Balance (KES)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        strokeWidth = 2.dp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatCurrency(balance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onInvestClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = "Invest",
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
                OutlinedButton(
                    onClick = onWithdrawClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("Withdraw")
                }
            }
        }
    }
}
