package com.example.g_kash.budget.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

// BRAND COLORS
private val GoldPremium = Color(0xFFFFD700)
private val PinkPremium = Color(0xFFFF1493)
private val WhitePure = Color(0xFFFFFFFF)
private val DarkText = Color(0xFF1A1A1A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSimulatorScreen(
    onNavigateBack: () -> Unit,
    onInvestClick: (Double) -> Unit
) {
    var incomeInput by remember { mutableStateOf("") }
    val incomeValue = incomeInput.toDoubleOrNull() ?: 0.0

    // 50-30-20 Calculations
    val needs = incomeValue * 0.5
    val wants = incomeValue * 0.3
    val savings = incomeValue * 0.2

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Simulator", fontWeight = FontWeight.Bold, color = DarkText) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DarkText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GoldPremium)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(WhitePure)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Simulate your financial future",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = PinkPremium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                "Enter an amount to see the 50-30-20 rule in action.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Income Input
            OutlinedTextField(
                value = incomeInput,
                onValueChange = { if (it.length <= 10) incomeInput = it },
                label = { Text("Total Income (KES)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                prefix = { Text("KES ") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkPremium,
                    focusedLabelColor = PinkPremium
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Results Section
            if (incomeValue > 0) {
                BudgetCategoryCard(
                    title = "Needs (50%)",
                    amount = needs,
                    description = "Rent, food, utilities, health.",
                    color = Color(0xFF2196F3),
                    icon = Icons.Default.Info
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                BudgetCategoryCard(
                    title = "Wants (30%)",
                    amount = wants,
                    description = "Dining out, entertainment, hobbies.",
                    color = Color(0xFFFF9800),
                    icon = Icons.Default.Star
                )

                Spacer(modifier = Modifier.height(16.dp))

                BudgetCategoryCard(
                    title = "Savings & Investment (20%)",
                    amount = savings,
                    description = "Building wealth for your future.",
                    color = PinkPremium,
                    icon = Icons.Default.Savings,
                    isHighlight = true
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Agentic AI Call to Action
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GoldPremium.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = PinkPremium, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Ready to make your money work?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = DarkText
                        )
                        Text(
                            "Your monthly investment potential is ${formatKes(savings)}.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onInvestClick(savings) },
                            colors = ButtonDefaults.buttonColors(containerColor = PinkPremium),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Start Investing Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Placeholder State
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("Results will appear here...", color = Color.LightGray)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun BudgetCategoryCard(
    title: String,
    amount: Double,
    description: String,
    color: Color,
    icon: ImageVector,
    isHighlight: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(if (isHighlight) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(containerColor = if (isHighlight) color.copy(alpha = 0.05f) else Color.Transparent),
        border = if (isHighlight) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = DarkText)
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(
                formatKes(amount),
                fontWeight = FontWeight.ExtraBold,
                color = color,
                fontSize = 16.sp
            )
        }
    }
}

private fun formatKes(amount: Double): String =
    "KES ${NumberFormat.getNumberInstance(Locale.US).format(amount)}"
