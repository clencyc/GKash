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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g_kash.ui.theme.PinkPrimary
import com.example.g_kash.ui.theme.GoldSecondary

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { /* Start Real Investment */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PinkPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Start Investing",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    OutlinedButton(
                        onClick = { /* Learn More */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PinkPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = PinkPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Learn More",
                            fontWeight = FontWeight.SemiBold
                        )
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
    val amounts = listOf(1000, 5000, 10000, 25000, 50000)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Investment Amount",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                amounts.forEach { amount ->
                    FilterChip(
                        onClick = { onAmountSelected(amount) },
                        label = { 
                            Text(
                                text = if (amount >= 1000) "${amount/1000}K" else "$amount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            ) 
                        },
                        selected = selectedAmount == amount,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PinkPrimary.copy(alpha = 0.15f),
                            selectedLabelColor = PinkPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Investment Period",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                periods.zip(periodLabels).forEach { (period, label) ->
                    FilterChip(
                        onClick = { onPeriodSelected(period) },
                        label = { 
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            ) 
                        },
                        selected = selectedPeriod == period,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldSecondary.copy(alpha = 0.15f),
                            selectedLabelColor = GoldSecondary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Risk Level",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                riskLevels.forEachIndexed { index, risk ->
                    FilterChip(
                        onClick = { onRiskSelected(risk) },
                        label = { 
                            Text(
                                text = risk,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            ) 
                        },
                        selected = selectedRisk == risk,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = riskColors[index].copy(alpha = 0.15f),
                            selectedLabelColor = riskColors[index],
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
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
            containerColor = PinkPrimary.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Estimated Portfolio Value",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "KES ${String.format("%,.0f", estimatedReturn)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PinkPrimary
            )
            
            Text(
                text = "after ${period} months",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Initial",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "KES ${String.format("%,d", initialAmount)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Profit",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "+KES ${String.format("%,.0f", estimatedReturn - initialAmount)}",
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