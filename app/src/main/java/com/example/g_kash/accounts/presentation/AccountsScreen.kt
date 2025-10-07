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
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.*

// --- FIX 1: DEFINE THE MISSING AccountType ENUM ---
// This enum holds display information and the raw string value the API expects.
enum class AccountType(val displayName: String, val apiValue: String) {
    BALANCED_FUND("Balanced Fund", "balanced_fund"),
    FIXED_INCOME_FUND("Fixed Income Fund", "fixed_income_fund"),
    MONEY_MARKET_FUND("Money Market Fund", "money_market_fund"),
    STOCK_MARKET("Stock Market", "stock_market")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    // It's better practice to let the screen get its own ViewModel
    viewModel: AccountsViewModel = koinViewModel(),
    onNavigateToTransactions: (String) -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Accounts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
                .padding(16.dp)
        ) {
            // Total Balance Card
            WalletBalanceCard(
                balance = uiState.totalBalance,
                isLoading = uiState.isLoading
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
                                    // --- FIX 2: USE 'id' INSTEAD OF 'accountId' ---
                                    onNavigateToTransactions(account.id)
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
fun AccountCard(account: Account, onClick: () -> Unit) {
    // This helper function converts the raw string from the API back to our enum
    val accountTypeEnum = AccountType.values().find { it.apiValue == account.accountType } ?: AccountType.BALANCED_FUND

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
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = getAccountTypeColor(accountTypeEnum).copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = getAccountTypeIcon(accountTypeEnum),
                        contentDescription = null,
                        tint = getAccountTypeColor(accountTypeEnum)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatAccountType(accountTypeEnum),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatCurrency(account.accountBalance),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "View Details", tint = MaterialTheme.colorScheme.outline)
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
fun WalletBalanceCard(balance: Double, isLoading: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Total Balance (Ksh)", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = formatCurrency(balance), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { /* Handle Save */ }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))) {
                    Text("Save", color = Color.Black)
                }
                Button(onClick = { /* Handle Withdraw */ }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242))) {
                    Text("Withdraw", color = Color.White)
                }
            }
        }
    }
}
