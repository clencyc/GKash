package com.example.g_kash.payment.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g_kash.accounts.data.Account
import com.example.g_kash.payment.data.PaymentReceipt
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.NumberFormat
import java.util.Locale

// BRAND-MATCHED COLORS
private val GoldPremium = Color(0xFFFFD700)
private val PinkPremium = Color(0xFFFF1493) // Vivid Magenta-Pink from logo
private val WhitePure = Color(0xFFFFFFFF)
private val DarkText = Color(0xFF1A1A1A)

// USER SPECIFIED GOLD PALETTE
private val GoldSecondaryContainer = Color(0xFF795502)     // Dark Gold Container
private val OnGoldSecondaryContainer = Color(0xFFFFFFFF)   // White
private val GoldSecondaryContainerLight = Color(0xFFFFF8DC) // Light Gold Container
private val OnGoldSecondaryContainerLight = Color(0xFF795502) // Dark Gold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(
    preselectedAccountId: String = "",
    onNavigateBack: () -> Unit,
    onNavigateToReceipt: (PaymentReceipt) -> Unit,
    viewModel: PaymentViewModel = koinViewModel(parameters = { parametersOf(preselectedAccountId) })
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // Handle success → navigate to receipt
    LaunchedEffect(uiState.workflowState) {
        val state = uiState.workflowState
        if (state is PaymentWorkflowState.Success) {
            onNavigateToReceipt(state.receipt)
            viewModel.consumeSuccess()
        }
    }

    // Snackbar for errors
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val isSubmitting = uiState.workflowState is PaymentWorkflowState.Loading ||
        uiState.workflowState is PaymentWorkflowState.AwaitingSTK

    // --- New: Account Creation Dialog ---
    if (uiState.showCreateAccountDialog) {
        CreateAccountDialog(
            onDismiss = { viewModel.setShowCreateAccountDialog(false) },
            onCreate = { apiValue ->
                viewModel.createAccount(apiValue)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Deposit to GKash", 
                        color = DarkText, 
                        fontWeight = FontWeight.Bold 
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DarkText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WhitePure,
                    titleContentColor = DarkText
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Hero gradient banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(PinkPremium, PinkPremium.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "M-Pesa STK Push",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Funds reflect instantly upon approval",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(20.dp))

                // ── Section 1: Choose account ─────────────────────────────
                Text(
                    text = "Select Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(10.dp))

                if (uiState.isLoadingAccounts) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PinkPremium)
                    }
                } else if (uiState.accounts.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = "No investment accounts found. You need to create one before depositing.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.setShowCreateAccountDialog(true) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PinkPremium,
                                    contentColor = WhitePure
                                ),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Create Account")
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        uiState.accounts.forEach { account ->
                            AccountSelectCard(
                                account = account,
                                selected = account.id == uiState.selectedAccountId,
                                onClick = { viewModel.selectAccount(account) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Section 2: Phone number ───────────────────────────────
                Text(
                    text = "M-Pesa Phone Number",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = viewModel::onPhoneChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("e.g. 0712345678") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = GoldSecondaryContainer)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(16.dp))

                // ── Section 3: Amount ─────────────────────────────────────
                Text(
                    text = "Amount (KES)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::onAmountChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Enter amount") },
                    leadingIcon = {
                        Text(
                            "KSh",
                            modifier = Modifier.padding(start = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldSecondaryContainer
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(12.dp),
                    supportingText = { Text("Minimum deposit: KES 100") }
                )

                // Quick-amount chips
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("100", "500", "1000", "5000").forEach { preset ->
                        FilledTonalButton(
                            onClick = { viewModel.onAmountChanged(preset) },
                            enabled = !isSubmitting,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 12.dp, vertical = 6.dp
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(preset, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── STK awaiting card ─────────────────────────────────────
                AnimatedVisibility(
                    visible = uiState.workflowState is PaymentWorkflowState.AwaitingSTK,
                    enter = fadeIn(), exit = fadeOut()
                ) {
                    (uiState.workflowState as? PaymentWorkflowState.AwaitingSTK)?.let { stkState ->
                        AwaitingPaymentCard(message = stkState.message)
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // ── Error card ────────────────────────────────────────────
                AnimatedVisibility(
                    visible = uiState.workflowState is PaymentWorkflowState.Error,
                    enter = fadeIn(), exit = fadeOut()
                ) {
                    (uiState.workflowState as? PaymentWorkflowState.Error)?.let { errorState ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
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
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = errorState.message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // ── CTA Button ────────────────────────────────────────────
                Button(
                    onClick = viewModel::submitDeposit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = !isSubmitting &&
                        uiState.selectedAccountId.isNotBlank() &&
                        uiState.phone.isNotBlank() &&
                        uiState.amount.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPremium,
                        contentColor = DarkText
                    )
                ) {
                    when (val ws = uiState.workflowState) {
                        is PaymentWorkflowState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = DarkText,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Sending STK Push…", color = DarkText, fontWeight = FontWeight.SemiBold)
                        }
                        is PaymentWorkflowState.AwaitingSTK -> {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = DarkText)
                            Spacer(Modifier.width(8.dp))
                            Text("Waiting for M-Pesa…", color = DarkText, fontWeight = FontWeight.SemiBold)
                        }
                        else -> {
                            Text(
                                "Invest Now",
                                color = DarkText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Account selector card
// ─────────────────────────────────────────────────────────────

@Composable
private fun AccountSelectCard(
    account: Account,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) PinkPremium else Color.Transparent
    val bgColor = if (selected) PinkPremium.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick)
            .border(2.dp, borderColor, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(if (selected) 4.dp else 1.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (selected) PinkPremium else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.width(6.dp))
                if (selected) {
                    Text(
                        "Selected",
                        style = MaterialTheme.typography.labelSmall,
                        color = PinkPremium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = account.accountType.replace("_", " ").replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatKes(account.accountBalance),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) PinkPremium else MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Awaiting STK card with pulse animation
// ─────────────────────────────────────────────────────────────

@Composable
private fun AwaitingPaymentCard(message: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "scale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PinkPremium.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(PinkPremium),
                contentAlignment = Alignment.Center
            ) {
                Text("M", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    "Awaiting M-Pesa Approval",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PinkPremium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────

private fun formatKes(amount: Double): String =
    "KSh ${NumberFormat.getNumberInstance(Locale.US).format(amount)}"

// ─────────────────────────────────────────────────────────────
// Shared components for account creation (Standardized)
// ─────────────────────────────────────────────────────────────

private enum class AccountType(val displayName: String, val apiValue: String) {
    BALANCED_FUND("Balanced Fund", "balanced fund"),
    FIXED_INCOME_FUND("Fixed Income Fund", "bond fund"),
    MONEY_MARKET_FUND("Money Market Fund", "money_market_fund"),
    STOCK_MARKET("Stock Market", "equity fund")
}

@Composable
private fun CreateAccountDialog(
    onDismiss: () -> Unit,
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
                        Text(type.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedType?.let { type ->
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
