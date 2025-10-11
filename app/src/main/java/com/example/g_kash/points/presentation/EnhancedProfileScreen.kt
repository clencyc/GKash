package com.example.g_kash.points.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g_kash.profile.presentation.*
import com.example.g_kash.points.data.StockPurchase
import org.koin.androidx.compose.koinViewModel

@Composable
fun EnhancedProfileScreen(
    onLogout: () -> Unit,
    onNavigateToPointsStore: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    profileViewModel: ProfileViewModel = koinViewModel(),
    pointsViewModel: PointsViewModel = koinViewModel()
) {
    val profileUiState by profileViewModel.uiState.collectAsState()
    val pointsState by pointsViewModel.pointsUiState
    val storeState by pointsViewModel.stockStoreUiState
    
    LaunchedEffect(Unit) {
        pointsViewModel.loadUserPoints()
        pointsViewModel.loadAvailableStocks()
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Enhanced Profile Header with Points
            EnhancedProfileHeader(
                user = profileUiState.user,
                points = pointsState.userPoints?.availablePoints ?: 0,
                lifetimeEarned = pointsState.userPoints?.lifetimeEarned ?: 0,
                onEditProfile = { /* Handle edit profile */ },
                onViewPointsStore = onNavigateToPointsStore
            )
        }
        
        item {
            // Learning Achievement Card
            LearningAchievementCard(
                completedModules = pointsViewModel.getCompletedModulesCount(),
                totalPointsEarned = pointsViewModel.getTotalPointsEarned(),
                stockValue = pointsViewModel.getTotalStockValue()
            )
        }
        
        item {
            // Stock Holdings Section
            if (storeState.userPurchases.isNotEmpty()) {
                StockHoldingsSection(purchases = storeState.userPurchases)
            }
        }
        
        item {
            // Points Quick Actions
            PointsQuickActionsSection(onNavigateToPointsStore = onNavigateToPointsStore)
        }
        
        item {
            // Achievement/Stats Card
            AchievementCard(achievements = profileUiState.achievements)
        }
        
        item {
            Text(
                text = "Account Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        items(getAccountSettingsOptions(onNavigateToSettings, onNavigateToNotifications, onNavigateToSecurity)) { option ->
            SettingsOptionItem(
                option = option,
                onClick = option.onClick
            )
        }
        
        item {
            Text(
                text = "Support & Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        items(getSupportOptions(onNavigateToHelp)) { option ->
            SettingsOptionItem(
                option = option,
                onClick = option.onClick
            )
        }
        
        item {
            // App Theme Toggle
            AppThemeSection()
        }
        
        item {
            // Logout Button
            LogoutSection(onLogout = onLogout)
        }
        
        item {
            // App version info
            AppVersionInfo()
        }
    }
}

@Composable
fun EnhancedProfileHeader(
    user: UserProfile,
    points: Int,
    lifetimeEarned: Int,
    onEditProfile: () -> Unit,
    onViewPointsStore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Picture and Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.profilePicture.isNotEmpty()) {
                            Text(
                                text = user.name.take(2).uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Points Display Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = "Points",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$points",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Available Points",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "$lifetimeEarned earned total",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }
                    
                    Button(
                        onClick = onViewPointsStore,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("View Store")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = "Store",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Edit Profile Button
            OutlinedButton(
                onClick = onEditProfile,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile")
            }
        }
    }
}

@Composable
fun LearningAchievementCard(
    completedModules: Int,
    totalPointsEarned: Int,
    stockValue: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Learning",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Learning Journey",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AchievementStatItem(
                    label = "Modules\nCompleted",
                    value = "$completedModules",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF4CAF50)
                )
                
                AchievementStatItem(
                    label = "Points\nEarned",
                    value = "$totalPointsEarned",
                    icon = Icons.Default.Stars,
                    color = Color(0xFFFFD700)
                )
                
                AchievementStatItem(
                    label = "Stock\nValue",
                    value = "$${String.format("%.1f", stockValue)}",
                    icon = Icons.Default.TrendingUp,
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

@Composable
fun AchievementStatItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color.copy(alpha = 0.15f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = color,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
            lineHeight = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun StockHoldingsSection(purchases: List<StockPurchase>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = "Stocks",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Stock Holdings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(purchases) { purchase ->
                    StockHoldingCard(purchase = purchase)
                }
            }
        }
    }
}

@Composable
fun StockHoldingCard(purchase: StockPurchase) {
    Card(
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = purchase.stockSymbol,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = purchase.stockSymbol,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Text(
                text = "${purchase.sharesAmount} shares",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Text(
                text = "$${String.format("%.2f", purchase.currentValue)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun PointsQuickActionsSection(
    onNavigateToPointsStore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Points & Learning",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Default.Store,
                    label = "Points Store",
                    onClick = onNavigateToPointsStore
                )
                
                QuickActionButton(
                    icon = Icons.Default.School,
                    label = "Learn More",
                    onClick = { /* Navigate to learning */ }
                )
                
                QuickActionButton(
                    icon = Icons.Default.History,
                    label = "History",
                    onClick = { /* Navigate to points history */ }
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}