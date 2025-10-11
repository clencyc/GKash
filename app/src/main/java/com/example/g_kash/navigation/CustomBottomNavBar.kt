package com.example.g_kash.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun CustomBottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home
            BottomNavIconButton(
                icon = Icons.Filled.Home,
                label = "Home",
                isSelected = currentRoute == "main/home",
                onClick = {
                    navController.navigate("main/home") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            // Learn
            BottomNavIconButton(
                icon = Icons.Filled.School,
                label = "Learn",
                isSelected = currentRoute == "main/learn",
                onClick = {
                    navController.navigate("main/learn") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            // Investment Simulator - Central Big Button
            InvestmentSimulatorButton(
                isSelected = currentRoute == "main/investment_simulator",
                onClick = {
                    navController.navigate("main/investment_simulator") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            // Chat
            BottomNavIconButton(
                icon = Icons.Filled.Chat,
                label = "Chat",
                isSelected = currentRoute == "main/chat",
                onClick = {
                    navController.navigate("main/chat") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            // Profile
            BottomNavIconButton(
                icon = Icons.Filled.Person,
                label = "Profile",
                isSelected = currentRoute == "main/profile",
                onClick = {
                    navController.navigate("main/profile") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavIconButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = tween(200),
        label = "nav_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .scale(scale)
            .padding(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(28.dp),
            tint = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun InvestmentSimulatorButton(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(200),
        label = "center_button_scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // Outer ring - lighter shade
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF4DD0E1).copy(alpha = 0.3f),
                            Color(0xFF00ACC1).copy(alpha = 0.6f)
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // Middle ring
        Box(
            modifier = Modifier
                .size(68.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF26C6DA),
                            Color(0xFF00ACC1)
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // Inner button - main green color
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF4CAF50),
                            Color(0xFF2E7D32)
                        )
                    ),
                    shape = CircleShape
                )
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = CircleShape
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = "Game",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "GAME",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}