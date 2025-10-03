
package com.example.g_kash.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.g_kash.authentication.presentation.AuthViewModel
import com.example.g_kash.authentication.presentation.HomeScreen
import com.example.g_kash.authentication.presentation.LearnScreen
import com.example.g_kash.authentication.presentation.Navigation.BottomNavItem
import com.example.g_kash.authentication.presentation.Navigation.Destination
import com.example.g_kash.authentication.presentation.ProfileScreen
import com.example.g_kash.authentication.presentation.WalletScreen
import org.koin.androidx.compose.koinViewModel

// ============================================
// MAIN APP SCAFFOLD WITH BOTTOM NAVIGATION
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    rootNavController: NavHostController, // For navigating outside bottom nav
    authViewModel: AuthViewModel = koinViewModel()
) {
    // Separate nav controller for bottom nav screens
    val bottomNavController = rememberNavController()
    var selectedItem by remember { mutableStateOf(BottomNavItem.Home) }

    // Sync selected item with current destination
    LaunchedEffect(bottomNavController) {
        bottomNavController.currentBackStackEntryFlow.collect { backStackEntry ->
            when (backStackEntry?.destination?.route) {
                Destination.Home.route -> selectedItem = BottomNavItem.Home
                Destination.Wallet.route -> selectedItem = BottomNavItem.Wallet
                Destination.Learn.route -> selectedItem = BottomNavItem.learn
                Destination.Profile.route -> selectedItem = BottomNavItem.profile
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomNavItem.entries.forEach { item ->
                    NavigationBarItem(
                        selected = selectedItem == item,
                        onClick = {
                            if (selectedItem != item) {
                                // Map BottomNavItem to Destination route
                                val destinationRoute = when (item) {
                                    BottomNavItem.Home -> Destination.Home.route
                                    BottomNavItem.Wallet -> Destination.Wallet.route
                                    BottomNavItem.learn -> Destination.Learn.route
                                    BottomNavItem.profile -> Destination.Profile.route
                                }

                                bottomNavController.navigate(destinationRoute) {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                                selectedItem = item
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selectedItem == item) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = Destination.Home.route,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Bottom navigation screens
            composable(Destination.Home.route) {
                HomeScreen(
                    onNavigateToSendMoney = { rootNavController.navigate(Destination.SendMoney.route) },
                    onNavigateToReceiveMoney = { rootNavController.navigate(Destination.ReceiveMoney.route) }
                )
            }
            composable(Destination.Wallet.route) {
                WalletScreen(
                    onNavigateToTransactionHistory = { rootNavController.navigate(Destination.TransactionHistory.route) }
                )
            }
            composable(Destination.Learn.route) {
                LearnScreen(
                    onNavigateToCourses = { rootNavController.navigate(Destination.Courses.route) }
                )
            }
            composable(Destination.Profile.route) {
                ProfileScreen(
                    onNavigateToSettings = { rootNavController.navigate(Destination.Settings.route) },
                    onNavigateToEditProfile = { rootNavController.navigate(Destination.EditProfile.route) },
                    onLogout = {
                        authViewModel.logout()
                        rootNavController.navigate(Destination.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
