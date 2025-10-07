package com.example.g_kash.accounts.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsScreen(
    accountId: String,
    // It's better practice to let the screen get its own ViewModel
    viewModel: AccountsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToTransactions: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val account = uiState.accounts.find { it.id == accountId }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMoreOptions by remember { mutableStateOf(false) }

    // Re-load accounts if they are not present, ensuring data is fresh.
    LaunchedEffect(Unit) {
        if (uiState.accounts.isEmpty()) {
            viewModel.loadUserAccounts()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMoreOptions = !showMoreOptions }) {
                        Icon(Icons.Default.MoreVert, "More Options")
                    }
                    DropdownMenu(
                        expanded = showMoreOptions,
                        onDismissRequest = { showMoreOptions = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Account") },
                            onClick = {
                                showMoreOptions = false
                                showDeleteDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (account == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Account not found", style = MaterialTheme.typography.titleLarge)
                        Text("It may have been deleted.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            // The call to AccountType.values() will now use the enum from the new helper file.
            val accountTypeEnum = AccountType.values().find { it.apiValue == account.accountType } ?: AccountType.BALANCED_FUND

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Account Balance Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        // The call to getAccountTypeColor now unambiguously resolves to the function in AccountUIHelpers.kt
                        containerColor = getAccountTypeColor(accountTypeEnum)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = getAccountTypeIcon(accountTypeEnum),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = formatAccountType(accountTypeEnum),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Current Balance", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            // The call to formatCurrency now unambiguously resolves to the function in AccountUIHelpers.kt
                            text = formatCurrency(account.accountBalance),
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Account Information
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Account Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    InfoRow(label = "Account ID", value = account.id)
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    InfoRow(label = "Account Type", value = formatAccountType(accountTypeEnum))
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    InfoRow(label = "Created", value = account.createdAt)
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    InfoRow(label = "Last Updated", value = account.updatedAt)
                }

                Spacer(modifier = Modifier.weight(1f)) // Pushes button to bottom

                // View Transactions Button
                Button(
                    onClick = onNavigateToTransactions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.History, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Transactions")
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Account?") },
            text = { Text("Are you sure you want to delete this account? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccount(accountId)
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// InfoRow does not need changes
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
