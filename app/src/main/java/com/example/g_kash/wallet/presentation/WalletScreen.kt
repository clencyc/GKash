package com.example.g_kash.wallet.presentation

// ... your other imports
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

import com.example.g_kash.accounts.presentation.AccountSummaryCard
import com.example.g_kash.accounts.presentation.SectionHeader
import com.example.g_kash.accounts.presentation.WalletBalanceCard
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    // These navigation callbacks are perfect. Do not change them.
    onNavigateToTransactionHistory: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToAccountDetails: (accountId: String) -> Unit,
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
        topBar = { /* ... your TopAppBar code ... */ }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Your UI should now use the new uiState from WalletViewModel
            WalletBalanceCard(
                balance = uiState.totalBalance,
                isLoading = uiState.isLoading,
            )

            // ... other UI sections ...

            // My Accounts Section
            SectionHeader(
                title = "My Accounts",
                actionText = "View All",
                onActionClick = onNavigateToAccounts // This is correct
            )

            if (uiState.isLoading && uiState.accounts.isEmpty()) {
                // ... loading indicator ...
            } else if (uiState.accounts.isEmpty()) {
                // ... empty state card ...
            } else {
                LazyRow(/*...*/) {
                    items(uiState.accounts) { account -> // Use accounts from WalletUiState
                        AccountSummaryCard(
                            account = account,
                            onClick = { onNavigateToAccountDetails(account.id) }
                        )
                    }
                }
            }
        }
    }
}

