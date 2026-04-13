package com.example.g_kash.investment.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.g_kash.investment.data.InvestmentOption
import com.example.g_kash.investment.data.InvestmentOptionId
import com.example.g_kash.investment.data.InvestmentReceipt
import com.example.g_kash.investment.data.InvestmentStep
import com.example.g_kash.investment.data.InvestmentWorkflowState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReceipt: (InvestmentReceipt) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InvestmentViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = if (uiState.workflowState is InvestmentWorkflowState.Error) "Retry" else null,
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.retryLastInvestment()
        }
        viewModel.clearSnackbarMessage()
    }

    LaunchedEffect(uiState.workflowState) {
        val workflowState = uiState.workflowState
        if (workflowState is InvestmentWorkflowState.Success) {
            onNavigateToReceipt(workflowState.receipt)
            viewModel.consumeSuccessState()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Investment Scout", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (uiState.step) {
                InvestmentStep.SELECT_OPTION -> {
                    item {
                        Text(
                            text = "Choose an investment option",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "GKash Savings is live. The other products are coming soon.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(uiState.options) { option ->
                        InvestmentOptionCard(
                            option = option,
                            selected = uiState.selectedOption.id == option.id,
                            onClick = { viewModel.selectOption(option) }
                        )
                    }

                    item {
                        Button(
                            onClick = viewModel::goToDetailsStep,
                            enabled = uiState.selectedOption.id == InvestmentOptionId.GKASH_SAVINGS,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Continue")
                        }
                    }
                }

                InvestmentStep.ENTER_DETAILS -> {
                    item {
                        Text(
                            text = uiState.selectedOption.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enter your phone number and amount to continue.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = uiState.phoneNumber,
                            onValueChange = viewModel::onPhoneChanged,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Phone number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = uiState.amount,
                            onValueChange = viewModel::onAmountChanged,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Amount to invest") },
                            leadingIcon = { Icon(Icons.Default.TrendingUp, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            supportingText = { Text("Minimum amount depends on the product") }
                        )
                    }

                    item {
                        val workflowState = uiState.workflowState
                        if (workflowState is InvestmentWorkflowState.AwaitingSTK) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.size(12.dp))
                                    Text(text = workflowState.message, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }

                    item {
                        if (uiState.workflowState is InvestmentWorkflowState.Error) {
                            val errorMessage = (uiState.workflowState as InvestmentWorkflowState.Error).message
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = errorMessage,
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = viewModel::goBackToOptionsStep,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back")
                            }

                            Button(
                                onClick = viewModel::submitInvestment,
                                enabled = uiState.selectedOption.id == InvestmentOptionId.GKASH_SAVINGS &&
                                    uiState.amount.toDoubleOrNull()?.let { it > 0.0 } == true &&
                                    uiState.phoneNumber.isNotBlank() &&
                                    uiState.workflowState !is InvestmentWorkflowState.Loading &&
                                    uiState.workflowState !is InvestmentWorkflowState.AwaitingSTK,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = when (uiState.workflowState) {
                                        is InvestmentWorkflowState.Loading -> "Starting STK Push..."
                                        is InvestmentWorkflowState.AwaitingSTK -> "Waiting for confirmation..."
                                        else -> "Invest Now"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvestmentOptionCard(
    option: InvestmentOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    val cardModifier = if (option.comingSoon) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    }

    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(
            containerColor = if (option.comingSoon) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (selected) BorderStroke(2.dp, option.accentColor) else null,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(option.accentColor.copy(alpha = 0.16f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = option.accentColor
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = option.title, fontWeight = FontWeight.SemiBold)
                    if (selected && !option.comingSoon) {
                        Spacer(modifier = Modifier.size(8.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = option.accentColor)
                    }
                }
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (option.comingSoon) {
                AssistChip(onClick = { }, label = { Text("Coming Soon") })
            }
        }
    }
}
