package com.example.g_kash.points.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g_kash.core.presentation.LearningModule
import com.example.g_kash.core.presentation.ModuleStatus
import com.example.g_kash.points.data.RewardDifficulty
import org.koin.androidx.compose.koinViewModel

// Enhanced learning module card that shows points rewards
@Composable
fun EnhancedLearningModuleCard(
    module: LearningModule,
    pointsReward: Int = 0,
    difficulty: RewardDifficulty = RewardDifficulty.BEGINNER,
    onClick: () -> Unit,
    onComplete: () -> Unit = {},
    pointsViewModel: PointsViewModel = koinViewModel()
) {
    var showCompletionDialog by remember { mutableStateOf(false) }
    val isCompleted = module.status == ModuleStatus.COMPLETED
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                if (module.status != ModuleStatus.LOCKED) {
                    onClick() 
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompleted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                module.status == ModuleStatus.IN_PROGRESS -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCompleted) 8.dp else 4.dp
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isCompleted) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        } else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row with status and points
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Module status indicator
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            when (module.status) {
                                ModuleStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                                ModuleStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
                                ModuleStatus.LOCKED -> MaterialTheme.colorScheme.outline
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (module.status) {
                            ModuleStatus.COMPLETED -> Icons.Default.CheckCircle
                            ModuleStatus.IN_PROGRESS -> Icons.Default.PlayCircleOutline
                            ModuleStatus.LOCKED -> Icons.Default.Lock
                        },
                        contentDescription = module.status.name,
                        tint = when (module.status) {
                            ModuleStatus.COMPLETED -> MaterialTheme.colorScheme.onPrimary
                            ModuleStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onSecondary
                            ModuleStatus.LOCKED -> MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Points reward badge
                if (pointsReward > 0 && !isCompleted) {
                    PointsRewardBadge(
                        points = pointsReward,
                        difficulty = difficulty
                    )
                } else if (isCompleted) {
                    CompletedBadge()
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Module info
            Text(
                text = module.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (module.status == ModuleStatus.LOCKED) 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = module.description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (module.status == ModuleStatus.LOCKED)
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Bottom row with duration and action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Duration and lessons info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Duration",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${module.duration} • ${module.lessons.size} lessons",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                
                // Action button
                when (module.status) {
                    ModuleStatus.COMPLETED -> {
                        TextButton(
                            onClick = { showCompletionDialog = true }
                        ) {
                            Text("View Details")
                        }
                    }
                    ModuleStatus.IN_PROGRESS -> {
                        Button(
                            onClick = { showCompletionDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Complete")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = "Points",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    ModuleStatus.LOCKED -> {
                        Text(
                            text = "Locked",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
    
    // Completion Dialog
    if (showCompletionDialog) {
        ModuleCompletionDialog(
            module = module,
            pointsReward = pointsReward,
            onDismiss = { showCompletionDialog = false },
            onComplete = { score ->
                pointsViewModel.completeModule(module.id, score)
                onComplete()
                showCompletionDialog = false
            }
        )
    }
}

@Composable
fun PointsRewardBadge(
    points: Int,
    difficulty: RewardDifficulty
) {
    val badgeColor = when (difficulty) {
        RewardDifficulty.BEGINNER -> Color(0xFF4CAF50)
        RewardDifficulty.INTERMEDIATE -> Color(0xFF2196F3)
        RewardDifficulty.ADVANCED -> Color(0xFF9C27B0)
        RewardDifficulty.EXPERT -> Color(0xFFFF9800)
    }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = badgeColor.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = "Points",
                tint = badgeColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "+$points",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = badgeColor
            )
        }
    }
}

@Composable
fun CompletedBadge() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Completed",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ModuleCompletionDialog(
    module: LearningModule,
    pointsReward: Int,
    onDismiss: () -> Unit,
    onComplete: (score: Float) -> Unit
) {
    var selectedScore by remember { mutableStateOf(1.0f) }
    val scores = listOf(
        0.6f to "Basic (60%)",
        0.8f to "Good (80%)",
        1.0f to "Excellent (100%)",
        1.2f to "Perfect (Bonus!)"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Complete",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (module.status == ModuleStatus.COMPLETED) "Module Details" else "Complete Module",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    text = module.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = module.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                if (module.status != ModuleStatus.COMPLETED) {
                    Text(
                        text = "How well did you understand this module?",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    scores.forEach { (score, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedScore = score }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedScore == score,
                                onClick = { selectedScore = score }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label)
                            Spacer(modifier = Modifier.weight(1f))
                            
                            // Show points calculation
                            val calculatedPoints = (pointsReward * score).toInt()
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = "Points",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "+$calculatedPoints",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } else {
                    // Show completion stats
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "✅ Module Completed!",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Points earned: +$pointsReward",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (module.status != ModuleStatus.COMPLETED) {
                Button(
                    onClick = { onComplete(selectedScore) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Complete Module")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = "Points",
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        },
        dismissButton = {
            if (module.status != ModuleStatus.COMPLETED) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

// Points celebration animation component
@Composable
fun PointsEarnedNotification(
    points: Int,
    moduleName: String,
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (visible) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50).copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = "Points earned",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "+$points Points Earned!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Completed: $moduleName",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White
                    )
                }
            }
        }
    }
}