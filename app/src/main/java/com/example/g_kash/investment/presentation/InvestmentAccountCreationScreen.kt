package com.example.g_kash.investment.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g_kash.investment.data.*
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentAccountCreationScreen(
    onNavigateBack: () -> Unit,
    onAccountCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InvestmentAccountCreationViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle successful account creation
    LaunchedEffect(uiState.isAccountCreated) {
        if (uiState.isAccountCreated) {
            onAccountCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = viewModel.getStepTitle(),
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentStep == InvestmentCreationStep.SELECT_TYPE) {
                            onNavigateBack()
                        } else {
                            viewModel.previousStep()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = { viewModel.getProgressPercentage() },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            
            // Error handling
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                }
            }

            // Main content
            when (uiState.currentStep) {
                InvestmentCreationStep.SELECT_TYPE -> {
                    InvestmentTypeSelectionStep(
                        selectedType = uiState.selectedAccountType,
                        onTypeSelected = viewModel::selectAccountType,
                        onNext = viewModel::nextStep
                    )
                }
                InvestmentCreationStep.SET_GOALS -> {
                    InvestmentGoalsStep(
                        selectedGoal = uiState.selectedGoal,
                        selectedHorizon = uiState.selectedHorizon,
                        onGoalSelected = viewModel::selectInvestmentGoal,
                        onHorizonSelected = viewModel::selectInvestmentHorizon,
                        onNext = viewModel::nextStep
                    )
                }
                InvestmentCreationStep.RISK_ASSESSMENT -> {
                    RiskAssessmentStep(
                        selectedRiskTolerance = uiState.selectedRiskTolerance,
                        onRiskToleranceSelected = viewModel::selectRiskTolerance,
                        onNext = viewModel::nextStep
                    )
                }
                InvestmentCreationStep.INITIAL_DEPOSIT -> {
                    InitialDepositStep(
                        accountType = uiState.selectedAccountType!!,
                        currentAmount = uiState.initialDeposit,
                        onAmountChanged = viewModel::setInitialDeposit,
                        onNext = viewModel::nextStep
                    )
                }
                InvestmentCreationStep.REVIEW_AND_CONFIRM -> {
                    ReviewAndConfirmStep(
                        uiState = uiState,
                        onConfirm = viewModel::nextStep,
                        isLoading = uiState.isLoading
                    )
                }
            }
        }
    }
}

@Composable
private fun InvestmentTypeSelectionStep(
    selectedType: InvestmentAccountType?,
    onTypeSelected: (InvestmentAccountType) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Choose the investment type that best fits your financial goals",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(InvestmentAccountType.values().toList()) { accountType ->
                InvestmentTypeCard(
                    accountType = accountType,
                    isSelected = selectedType == accountType,
                    onSelected = { onTypeSelected(accountType) }
                )
            }
        }

        Button(
            onClick = onNext,
            enabled = selectedType != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun InvestmentTypeCard(
    accountType: InvestmentAccountType,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = accountType.getIcon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = accountType.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                
                // Risk level badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accountType.riskLevel.getColor().copy(alpha = 0.2f)
                ) {
                    Text(
                        text = accountType.riskLevel.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = accountType.riskLevel.getColor()
                    )
                }
            }

            Text(
                text = accountType.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Min. Amount",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "KES ${NumberFormat.getNumberInstance(Locale.US).format(accountType.minimumAmount.toInt())}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Expected Return",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = accountType.expectedReturn,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun InvestmentGoalsStep(
    selectedGoal: InvestmentGoal?,
    selectedHorizon: InvestmentHorizon?,
    onGoalSelected: (InvestmentGoal) -> Unit,
    onHorizonSelected: (InvestmentHorizon) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "What are your investment goals?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "This helps us recommend the best investment strategy for you",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Investment Goal",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(InvestmentGoal.values().toList()) { goal ->
                SelectableCard(
                    title = goal.displayName,
                    description = goal.description,
                    isSelected = selectedGoal == goal,
                    onClick = { onGoalSelected(goal) },
                    icon = goal.getIcon()
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Investment Timeline",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(InvestmentHorizon.values().toList()) { horizon ->
                SelectableCard(
                    title = horizon.displayName,
                    description = horizon.description,
                    isSelected = selectedHorizon == horizon,
                    onClick = { onHorizonSelected(horizon) }
                )
            }
        }

        Button(
            onClick = onNext,
            enabled = selectedGoal != null && selectedHorizon != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun RiskAssessmentStep(
    selectedRiskTolerance: RiskTolerance?,
    onRiskToleranceSelected: (RiskTolerance) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "What's your risk tolerance?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Understanding your comfort level with risk helps us recommend suitable investments",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(RiskTolerance.values().toList()) { riskTolerance ->
                SelectableCard(
                    title = riskTolerance.displayName,
                    description = riskTolerance.description,
                    isSelected = selectedRiskTolerance == riskTolerance,
                    onClick = { onRiskToleranceSelected(riskTolerance) }
                )
            }
        }

        Button(
            onClick = onNext,
            enabled = selectedRiskTolerance != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun InitialDepositStep(
    accountType: InvestmentAccountType,
    currentAmount: Double,
    onAmountChanged: (Double) -> Unit,
    onNext: () -> Unit
) {
    var amountText by remember { mutableStateOf(if (currentAmount > 0) currentAmount.toString() else "") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Set your initial deposit",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Minimum deposit for ${accountType.displayName} is KES ${NumberFormat.getNumberInstance(Locale.US).format(accountType.minimumAmount.toInt())}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { newValue ->
                        amountText = newValue
                        newValue.toDoubleOrNull()?.let { amount ->
                            onAmountChanged(amount)
                        }
                    },
                    label = { Text("Initial Deposit Amount") },
                    prefix = { Text("KES ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Suggested amounts:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val suggestedAmounts = listOf(
                        accountType.minimumAmount,
                        accountType.minimumAmount * 2,
                        accountType.minimumAmount * 5
                    )
                    
                    suggestedAmounts.forEach { amount ->
                        SuggestionChip(
                            onClick = {
                                amountText = amount.toInt().toString()
                                onAmountChanged(amount)
                            },
                            label = { 
                                Text("KES ${NumberFormat.getNumberInstance(Locale.US).format(amount.toInt())}") 
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            enabled = currentAmount >= accountType.minimumAmount,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun ReviewAndConfirmStep(
    uiState: InvestmentAccountCreationUiState,
    onConfirm: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Review your investment account",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Please review the details below before creating your account",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                ReviewItem("Investment Type", uiState.selectedAccountType?.displayName ?: "")
                ReviewItem("Investment Goal", uiState.selectedGoal?.displayName ?: "")
                ReviewItem("Timeline", uiState.selectedHorizon?.displayName ?: "")
                ReviewItem("Risk Tolerance", uiState.selectedRiskTolerance?.displayName ?: "")
                ReviewItem(
                    "Initial Deposit", 
                    "KES ${NumberFormat.getNumberInstance(Locale.US).format(uiState.initialDeposit.toInt())}"
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onConfirm,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Create Investment Account")
            }
        }
    }
}

@Composable
private fun ReviewItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SelectableCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
        }
    }
}