package com.example.g_kash.core.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningPathScreen(
    categoryId: String,
    onNavigateBack: () -> Unit
) {
    val category = getLearningPathData(categoryId)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with back button
        LearningPathHeader(
            title = category.title,
            onNavigateBack = onNavigateBack
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Category overview card
                CategoryOverviewCard(category = category)
            }
            
            item {
                Text(
                    text = "Learning Modules",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(category.modules) { module ->
                LearningModuleCard(
                    module = module,
                    onClick = { /* Handle module click */ }
                )
            }
            
            item {
                Text(
                    text = "Practice Exercises",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(category.exercises) { exercise ->
                PracticeExerciseCard(
                    exercise = exercise,
                    onClick = { /* Handle exercise click */ }
                )
            }
            
            item {
                // Progress tracking
                ProgressSection(progress = category.progress)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningPathHeader(
    title: String,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun CategoryOverviewCard(category: LearningPath) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.title,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Modules",
                    value = "${category.modules.size}",
                    icon = Icons.Default.MenuBook
                )
                StatItem(
                    label = "Duration",
                    value = category.estimatedTime,
                    icon = Icons.Default.AccessTime
                )
                StatItem(
                    label = "Level",
                    value = category.difficulty,
                    icon = Icons.Default.TrendingUp
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun LearningModuleCard(
    module: LearningModule,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
                        RoundedCornerShape(8.dp)
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
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = module.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = module.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "${module.duration} • ${module.lessons.size} lessons",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            if (module.status != ModuleStatus.LOCKED) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open module",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PracticeExerciseCard(
    exercise: PracticeExercise,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Badge(
                    containerColor = when (exercise.type) {
                        ExerciseType.QUIZ -> MaterialTheme.colorScheme.primary
                        ExerciseType.CALCULATOR -> MaterialTheme.colorScheme.secondary
                        ExerciseType.SCENARIO -> MaterialTheme.colorScheme.tertiary
                    }
                ) {
                    Text(
                        text = exercise.type.name,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Text(
                text = exercise.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Duration",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = exercise.estimatedTime,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp)
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (exercise.isCompleted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressSection(progress: LearningProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Your Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Overall Progress",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(progress.completionPercentage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { progress.completionPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${progress.completedModules}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = progress.timeSpent,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Time Spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${progress.streak}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Day Streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Data classes for learning path
data class LearningPath(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val estimatedTime: String,
    val difficulty: String,
    val modules: List<LearningModule>,
    val exercises: List<PracticeExercise>,
    val progress: LearningProgress
)

data class LearningModule(
    val id: String,
    val title: String,
    val description: String,
    val duration: String,
    val lessons: List<Lesson>,
    val status: ModuleStatus
)

data class Lesson(
    val id: String,
    val title: String,
    val content: String,
    val type: LessonType
)

data class PracticeExercise(
    val id: String,
    val title: String,
    val description: String,
    val estimatedTime: String,
    val type: ExerciseType,
    val isCompleted: Boolean
)

data class LearningProgress(
    val completionPercentage: Float,
    val completedModules: Int,
    val totalModules: Int,
    val timeSpent: String,
    val streak: Int
)

enum class ModuleStatus { COMPLETED, IN_PROGRESS, LOCKED }
enum class LessonType { VIDEO, ARTICLE, INTERACTIVE }
enum class ExerciseType { QUIZ, CALCULATOR, SCENARIO }

// Mock data function
fun getLearningPathData(categoryId: String): LearningPath {
    return when (categoryId) {
        "get_started" -> LearningPath(
            id = "get_started",
            title = "Get Started",
            description = "Learn the basics of personal finance and money management",
            icon = Icons.Default.Home,
            estimatedTime = "2 hours",
            difficulty = "Beginner",
            modules = listOf(
                LearningModule(
                    id = "basics",
                    title = "Financial Basics",
                    description = "Understanding money, income, and expenses",
                    duration = "30 min",
                    lessons = listOf(),
                    status = ModuleStatus.COMPLETED
                ),
                LearningModule(
                    id = "goals",
                    title = "Setting Financial Goals",
                    description = "How to set and achieve your money goals",
                    duration = "25 min",
                    lessons = listOf(),
                    status = ModuleStatus.IN_PROGRESS
                ),
                LearningModule(
                    id = "tracking",
                    title = "Tracking Your Money",
                    description = "Tools and methods to monitor your finances",
                    duration = "35 min",
                    lessons = listOf(),
                    status = ModuleStatus.LOCKED
                )
            ),
            exercises = listOf(
                PracticeExercise(
                    id = "budget_quiz",
                    title = "Budgeting Quiz",
                    description = "Test your understanding of basic budgeting concepts",
                    estimatedTime = "10 min",
                    type = ExerciseType.QUIZ,
                    isCompleted = true
                ),
                PracticeExercise(
                    id = "goal_calculator",
                    title = "Goal Planning Calculator",
                    description = "Calculate how much to save for your goals",
                    estimatedTime = "15 min",
                    type = ExerciseType.CALCULATOR,
                    isCompleted = false
                )
            ),
            progress = LearningProgress(
                completionPercentage = 0.4f,
                completedModules = 1,
                totalModules = 3,
                timeSpent = "45m",
                streak = 3
            )
        )
        
        "saving_basics" -> LearningPath(
            id = "saving_basics",
            title = "Saving Basics",
            description = "Master the art of saving money and building wealth",
            icon = Icons.Default.AccountBalance,
            estimatedTime = "3 hours",
            difficulty = "Beginner",
            modules = listOf(
                LearningModule(
                    id = "emergency_fund",
                    title = "Emergency Fund",
                    description = "Building your financial safety net",
                    duration = "40 min",
                    lessons = listOf(),
                    status = ModuleStatus.COMPLETED
                ),
                LearningModule(
                    id = "savings_strategies",
                    title = "Savings Strategies",
                    description = "Effective ways to save more money",
                    duration = "45 min",
                    lessons = listOf(),
                    status = ModuleStatus.IN_PROGRESS
                )
            ),
            exercises = listOf(
                PracticeExercise(
                    id = "emergency_calculator",
                    title = "Emergency Fund Calculator",
                    description = "Calculate your ideal emergency fund size",
                    estimatedTime = "10 min",
                    type = ExerciseType.CALCULATOR,
                    isCompleted = false
                )
            ),
            progress = LearningProgress(
                completionPercentage = 0.6f,
                completedModules = 1,
                totalModules = 2,
                timeSpent = "1h 20m",
                streak = 5
            )
        )
        
        // Add more categories as needed
        else -> LearningPath(
            id = categoryId,
            title = "Learning Path",
            description = "Expand your financial knowledge",
            icon = Icons.Default.School,
            estimatedTime = "1 hour",
            difficulty = "Beginner",
            modules = listOf(),
            exercises = listOf(),
            progress = LearningProgress(0f, 0, 0, "0m", 0)
        )
    }
}