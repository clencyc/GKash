package com.example.g_kash.investment.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Theme colors are now used directly via MaterialTheme.colorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentSimulatorScreen() {
    var selectedAmount by remember { mutableStateOf(1000) }
    var selectedPeriod by remember { mutableStateOf(12) }
    var selectedRisk by remember { mutableStateOf("Medium") }
    
    // Budget Simulator State
    var budgetAllowance by remember { mutableStateOf(50000.0) }
    var selectedBudgetRule by remember { mutableStateOf(BudgetRule.RULE_50_30_20) }
    var customNeeds by remember { mutableStateOf(50f) }
    var customWants by remember { mutableStateOf(30f) }
    var customSavings by remember { mutableStateOf(20f) }

    val estimatedReturn = calculateReturn(selectedAmount, selectedPeriod, selectedRisk)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Financial Simulator",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                SectionHeader("Investment Simulation", Icons.Default.TrendingUp)
            }
            
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
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
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
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary
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

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
            }

            item {
                SectionHeader("Budget Simulation", Icons.Default.PieChart)
            }

            item {
                BudgetSimulatorCard(
                    allowance = budgetAllowance,
                    onAllowanceChange = { budgetAllowance = it },
                    selectedRule = selectedBudgetRule,
                    onRuleSelected = { selectedBudgetRule = it },
                    customNeeds = customNeeds,
                    customWants = customWants,
                    customSavings = customSavings,
                    onCustomChange = { n, w, s ->
                        customNeeds = n
                        customWants = w
                        customSavings = s
                    }
                )
            }
            
            item {
                // Investment Leaderboard
                Spacer(modifier = Modifier.height(8.dp))
                InvestmentLeaderboard()
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
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
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
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
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
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
                            selectedContainerColor = riskColors[index].copy(alpha = 0.2f),
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
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
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
                color = MaterialTheme.colorScheme.primary
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

// Leaderboard data class
data class InvestmentLeader(
    val rank: Int,
    val anonymousName: String,
    val investmentAmount: Double,
    val returnAmount: Double,
    val riskLevel: String
)

@Composable
fun InvestmentLeaderboard() {
    val leaderboardData = remember { generateLeaderboardData() }
    
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Leaderboard,
                    contentDescription = "Leaderboard",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Top Investors This Month",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Leaderboard header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Rank",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.weight(0.8f)
                )
                Text(
                    text = "Investor",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.weight(2f)
                )
                Text(
                    text = "Amount",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    text = "Returns",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.5f)
                )
            }
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // Leaderboard items
            leaderboardData.forEach { leader ->
                LeaderboardItem(leader = leader)
                if (leader.rank < leaderboardData.size) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Footer note
            Text(
                text = "🏆 Compete with other investors and climb the leaderboard!",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun LeaderboardItem(leader: InvestmentLeader) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Rank without medals
        Box(
            modifier = Modifier.weight(0.8f),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "${leader.rank}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Anonymous name without avatar
        Column(
            modifier = Modifier.weight(2f)
        ) {
            Text(
                text = leader.anonymousName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${leader.riskLevel} Risk",
                fontSize = 10.sp,
                color = getRiskColor(leader.riskLevel),
                fontWeight = FontWeight.Medium
            )
        }
        
        // Investment amount
        Text(
            text = "${formatAmount(leader.investmentAmount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.5f)
        )
        
        // Return amount
        Text(
            text = "+${formatAmount(leader.returnAmount - leader.investmentAmount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSystemInDarkTheme()) Color(0xFF81C784) else Color(0xFF4CAF50),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.5f)
        )
    }
}

@Composable
private fun getRiskColor(riskLevel: String): Color {
    val isDark = isSystemInDarkTheme()
    return when (riskLevel) {
        "Low" -> if (isDark) Color(0xFF81C784) else Color(0xFF4CAF50)
        "Medium" -> MaterialTheme.colorScheme.tertiary
        "High" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.tertiary
    }
}

enum class BudgetRule(val label: String, val needs: Float, val wants: Float, val savings: Float) {
    RULE_50_30_20("50-30-20", 50f, 30f, 20f),
    RULE_70_20_10("70-20-10", 70f, 20f, 10f),
    RULE_60_20_20("60-20-20", 60f, 20f, 20f),
    CUSTOM("Custom", 0f, 0f, 0f)
}

@Composable
fun BudgetSimulatorCard(
    allowance: Double,
    onAllowanceChange: (Double) -> Unit,
    selectedRule: BudgetRule,
    onRuleSelected: (BudgetRule) -> Unit,
    customNeeds: Float,
    customWants: Float,
    customSavings: Float,
    onCustomChange: (Float, Float, Float) -> Unit
) {
    val needsPct = if (selectedRule == BudgetRule.CUSTOM) customNeeds else selectedRule.needs
    val wantsPct = if (selectedRule == BudgetRule.CUSTOM) customWants else selectedRule.wants
    val savingsPct = if (selectedRule == BudgetRule.CUSTOM) customSavings else selectedRule.savings

    val needsAmount = allowance * (needsPct / 100)
    val wantsAmount = allowance * (wantsPct / 100)
    val savingsAmount = allowance * (savingsPct / 100)

    val currentAllowanceStr = remember(allowance) { if (allowance == 0.0) "" else allowance.toInt().toString() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Monthly Allowance (KES)",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            OutlinedTextField(
                value = currentAllowanceStr,
                onValueChange = { onAllowanceChange(it.toDoubleOrNull() ?: 0.0) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                prefix = { Text("KES ") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )

            Text(
                "Budgeting Rule",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BudgetRule.values().forEach { rule ->
                    FilterChip(
                        selected = selectedRule == rule,
                        onClick = { onRuleSelected(rule) },
                        label = { Text(rule.label, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            if (selectedRule == BudgetRule.CUSTOM) {
                CustomBudgetSliders(
                    needs = customNeeds,
                    wants = customWants,
                    savings = customSavings,
                    onUpdate = onCustomChange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visual Breakdown
            BudgetVisualBreakdown(needsPct, wantsPct, savingsPct)

            Spacer(modifier = Modifier.height(24.dp))

            // Details
            BudgetDetailRow("Needs ($needsPct%)", needsAmount, MaterialTheme.colorScheme.primary)
            BudgetDetailRow("Wants ($wantsPct%)", wantsAmount, MaterialTheme.colorScheme.tertiary)
            BudgetDetailRow("Savings ($savingsPct%)", savingsAmount, MaterialTheme.colorScheme.secondary)

            // Future Wealth Projection
            WealthProjectionCard(monthlySavings = savingsAmount)
        }
    }
}

@Composable
fun CustomBudgetSliders(
    needs: Float,
    wants: Float,
    savings: Float,
    onUpdate: (Float, Float, Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        SliderWithLabel("Needs", needs) { onUpdate(it, wants, 100f - it - wants) }
        SliderWithLabel("Wants", wants) { onUpdate(needs, it, 100f - needs - it) }
        Text(
            "Savings will be automatically calculated to reach 100%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun SliderWithLabel(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text("${value.toInt()}%", fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..100f,
            modifier = Modifier.height(32.dp)
        )
    }
}

@Composable
fun BudgetVisualBreakdown(needs: Float, wants: Float, savings: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Box(modifier = Modifier.weight(needs.coerceAtLeast(1f)).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
        Box(modifier = Modifier.weight(wants.coerceAtLeast(1f)).fillMaxHeight().background(MaterialTheme.colorScheme.tertiary))
        Box(modifier = Modifier.weight(savings.coerceAtLeast(1f)).fillMaxHeight().background(MaterialTheme.colorScheme.secondary))
    }
}

@Composable
fun BudgetDetailRow(label: String, amount: Double, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
        Text(
            "KES ${String.format("%,.0f", amount)}",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun WealthProjectionCard(monthlySavings: Double) {
    val rate = 0.08 / 12 // 8% APY
    val projections = listOf(6, 12, 24)
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Potential Savings Growth", fontWeight = FontWeight.Bold)
            }
            Text("Assuming 8% annual returns on GKash", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                projections.forEach { months ->
                    val total = if (monthlySavings > 0) {
                        monthlySavings * (Math.pow(1 + rate, months.toDouble()) - 1) / rate
                    } else 0.0
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${months}mo", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "KES ${String.format("%,.0f", total)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    return when {
        amount >= 1000000 -> String.format("%.1fM", amount / 1000000)
        amount >= 1000 -> String.format("%.0fK", amount / 1000)
        else -> String.format("%.0f", amount)
    }
}

private fun generateLeaderboardData(): List<InvestmentLeader> {
    val anonymousNames = listOf(
        "CryptoKing47", "WealthBuilder", "InvestorPro", "MoneyMaven", "CapitalGuru",
        "ProfitHunter", "SmartTrader", "GrowthSeeker", "RiskTaker99", "FinanceWiz"
    )
    
    val riskLevels = listOf("Low", "Medium", "High")
    
    return (1..10).map { rank ->
        val baseAmount = when (rank) {
            1 -> (80000..150000).random().toDouble()
            in 2..3 -> (50000..80000).random().toDouble()
            in 4..6 -> (25000..50000).random().toDouble()
            else -> (5000..25000).random().toDouble()
        }
        
        val riskLevel = riskLevels.random()
        val returnMultiplier = when (riskLevel) {
            "Low" -> 1.05 + Math.random() * 0.10  // 1.05 to 1.15
            "Medium" -> 1.10 + Math.random() * 0.15  // 1.10 to 1.25
            "High" -> 1.20 + Math.random() * 0.20  // 1.20 to 1.40
            else -> 1.15
        }
        
        InvestmentLeader(
            rank = rank,
            anonymousName = anonymousNames.random(),
            investmentAmount = baseAmount,
            returnAmount = baseAmount * returnMultiplier,
            riskLevel = riskLevel
        )
    }.sortedByDescending { it.returnAmount - it.investmentAmount }
        .mapIndexed { index, leader -> leader.copy(rank = index + 1) }
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
