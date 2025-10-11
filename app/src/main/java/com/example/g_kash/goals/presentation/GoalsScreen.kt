package com.example.g_kash.goals.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen() {
    val sampleGoals = listOf(
        Goal("Emergency Fund", 5000.0, 1200.0),
        Goal("Vacation Savings", 3000.0, 800.0),
        Goal("New Car", 20000.0, 5500.0),
        Goal("Investment Portfolio", 10000.0, 2300.0)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Financial Goals",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            FloatingActionButton(
                onClick = { /* Add new goal */ },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Goal"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Goals List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sampleGoals) { goal ->
                GoalCard(goal = goal)
            }
        }
    }
}

@Composable
fun GoalCard(goal: Goal) {
    val progress = (goal.current / goal.target).coerceIn(0.0, 1.0)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goal.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                Icon(
                    imageVector = Icons.Default.TrackChanges,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "$${goal.current.toInt()} of $${goal.target.toInt()}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progress.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${(progress * 100).toInt()}% Complete",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

data class Goal(
    val name: String,
    val target: Double,
    val current: Double
)