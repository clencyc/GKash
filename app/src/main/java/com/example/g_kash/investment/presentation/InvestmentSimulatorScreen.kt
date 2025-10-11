package com.example.g_kash.investment.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentSimulatorScreen() {
    var selectedAmount by remember { mutableStateOf(1000) }
    var selectedPeriod by remember { mutableStateOf(12) }
    var selectedRisk by remember { mutableStateOf("Medium") }
    
    val estimatedReturn = calculateReturn(selectedAmount, selectedPeriod, selectedRisk)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Investment Simulator",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                // Investment Amount Selector
                InvestmentAmountCard(
                    selectedAmount = selectedAmount,
                    onAmountSelected = { selectedAmount = it }
                )
            }
            
            item {
                // Time Period Selector
                TimePeriodCard(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { selectedPeriod = it }
                )
            }
            
            item {
                // Risk Level Selector
                RiskLevelCard(
                    selectedRisk = selectedRisk,
                    onRiskSelected = { selectedRisk = it }
                )
            }
            
            item {
                // Results Card
                ResultsCard(
                    initialAmount = selectedAmount,
                    estimatedReturn = estimatedReturn,
                    period = selectedPeriod
                )
            }
            
            item {
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { /* Start Real Investment */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Start Investing")
                    }
                    
                    OutlinedButton(
                        onClick = { /* Learn More */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Learn More")
                    }
                }
            }
        }
    }
}

@Composable
fun InvestmentAmountCard(
    selectedAmount: Int,
    onAmountSelected: (Int) -> Unit
) {
    val amounts = listOf(500, 1000, 2500, 5000, 10000)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Investment Amount",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                amounts.forEach { amount ->
                    FilterChip(
                        onClick = { onAmountSelected(amount) },
                        label = { Text("$${amount}") },
                        selected = selectedAmount == amount,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun TimePeriodCard(
    selectedPeriod: Int,
    onPeriodSelected: (Int) -> Unit
) {
    val periods = listOf(6, 12, 24, 36, 60)
    val periodLabels = listOf("6mo", "1yr", "2yr", "3yr", "5yr")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Investment Period",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                periods.zip(periodLabels).forEach { (period, label) ->
                    FilterChip(
                        onClick = { onPeriodSelected(period) },
                        label = { Text(label) },
                        selected = selectedPeriod == period,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun RiskLevelCard(
    selectedRisk: String,
    onRiskSelected: (String) -> Unit
) {
    val riskLevels = listOf("Low", "Medium", "High")
    val riskColors = listOf(
        Color(0xFF4CAF50),
        Color(0xFFFF9800), 
        Color(0xFFF44336)
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Risk Level",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                riskLevels.forEachIndexed { index, risk ->
                    FilterChip(
                        onClick = { onRiskSelected(risk) },
                        label = { Text(risk) },
                        selected = selectedRisk == risk,
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = riskColors[index].copy(alpha = 0.2f),
                            selectedLabelColor = riskColors[index]
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ResultsCard(
    initialAmount: Int,
    estimatedReturn: Double,
    period: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Estimated Portfolio Value",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "$${String.format("%.2f", estimatedReturn)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "after ${period} months",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Initial",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$${initialAmount}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Profit",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "+$${String.format("%.0f", estimatedReturn - initialAmount)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

private fun calculateReturn(amount: Int, months: Int, risk: String): Double {
    val annualRate = when (risk) {
        "Low" -> 0.05
        "Medium" -> 0.08
        "High" -> 0.12
        else -> 0.08
    }
    
    val monthlyRate = annualRate / 12
    return amount * Math.pow(1 + monthlyRate, months.toDouble())
}