package com.example.g_kash.transactions.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.g_kash.accounts.presentation.formatCurrency
import com.example.g_kash.transactions.data.TransactionType
import com.example.g_kash.wallet.presentation.WalletTransactionCard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
	onNavigateBack: () -> Unit,
	viewModel: TransactionsViewModel = koinViewModel()
) {
	val uiState by viewModel.uiState.collectAsState()

	LaunchedEffect(Unit) {
		viewModel.loadAllTransactions()
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Transactions") },
				navigationIcon = {
					IconButton(onClick = onNavigateBack) {
						Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
				.padding(16.dp)
		) {
			Card(
				modifier = Modifier.fillMaxWidth(),
				colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
			) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Column {
						Text("Current balance", style = MaterialTheme.typography.bodySmall)
						Text(
							text = formatCurrency(uiState.currentBalance),
							style = MaterialTheme.typography.headlineSmall,
							fontWeight = FontWeight.Bold
						)
					}
					Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
				}
			}

			Spacer(modifier = Modifier.height(16.dp))

			if (uiState.isLoading) {
				CircularProgressIndicator()
				return@Column
			}

			uiState.error?.let { errorMessage ->
				Text(
					text = errorMessage,
					color = MaterialTheme.colorScheme.error,
					style = MaterialTheme.typography.bodyMedium
				)
				Spacer(modifier = Modifier.height(16.dp))
			}

			if (uiState.transactions.isEmpty()) {
				Text(
					text = "No transactions yet",
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			} else {
				LazyColumn(
					contentPadding = PaddingValues(bottom = 24.dp),
					verticalArrangement = Arrangement.spacedBy(12.dp)
				) {
					items(uiState.transactions) { transaction ->
						WalletTransactionCard(transaction = transaction, onClick = { })
					}
				}
			}
		}
	}
}