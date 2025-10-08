package com.example.g_kash.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.g_kash.accounts.presentation.AccountDetailsScreen
import com.example.g_kash.accounts.presentation.AccountsScreen
import com.example.g_kash.authentication.presentation.*
import com.example.g_kash.core.presentation.LearnScreen
import com.example.g_kash.core.presentation.ProfileScreen
import com.example.g_kash.chat.presentation.ChatScreen
import com.example.g_kash.profile.presentation.ProfileScreen as ActualProfileScreen
import com.example.g_kash.core.presentation.LearningPathScreen
import com.example.g_kash.authentication.presentation.ImprovedCreateAccountScreen
import com.example.g_kash.authentication.presentation.ImprovedCreatePinScreen
import com.example.g_kash.authentication.presentation.ImprovedConfirmPinScreen
import com.example.g_kash.transactions.data.AccountTransactionsScreen
import com.example.g_kash.wallet.presentation.WalletScreen
import org.koin.androidx.compose.koinViewModel

// Main navigation setup for the entire application
@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = koinViewModel()
    val uiAuthState by authViewModel.uiAuthState.collectAsState()
    val navController = rememberNavController()

    // Screens that should show the bottom bar.
    val bottomBarScreens = listOf(
        BottomNavItem.HOME.route,
        BottomNavItem.LEARN.route,
        BottomNavItem.CHAT.route,
        BottomNavItem.PROFILE.route,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val shouldShowBottomBar = currentRoute in bottomBarScreens

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        // Show a loading screen while checking auth state
        if (uiAuthState is UiAuthState.Unknown) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val startDestination = if (uiAuthState is UiAuthState.Authenticated) Graph.MAIN else Graph.AUTH

        // --- THE SINGLE NavHost FOR THE ENTIRE APP ---
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- AUTHENTICATION GRAPH ---
            navigation(
                startDestination = "auth/login",
                route = Graph.AUTH
            ) {
                composable("auth/login") {
                    LoginScreen(
                        onNavigateToSignup = { navController.navigate("auth/signup") },
                        onLoginSuccess = {
                            navController.navigate(Graph.MAIN) {
                                popUpTo(Graph.AUTH) { inclusive = true }
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable("auth/signup") {
                    ImprovedCreateAccountScreen(
                        onNavigateToPin = { name, phone, idNumber ->
                            navController.navigate("auth/create_pin/$name/$phone/$idNumber")
                        },
                        onNavigateToLogin = {
                            navController.navigate("auth/login") {
                                popUpTo("auth/signup") { inclusive = true }
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable(
                    route = "auth/create_pin/{name}/{phone}/{idNumber}",
                    arguments = listOf(
                        navArgument("name") { type = NavType.StringType },
                        navArgument("phone") { type = NavType.StringType },
                        navArgument("idNumber") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val name = backStackEntry.arguments?.getString("name") ?: ""
                    val phone = backStackEntry.arguments?.getString("phone") ?: ""
                    val idNumber = backStackEntry.arguments?.getString("idNumber") ?: ""
                    
                    ImprovedCreatePinScreen(
                        userName = name,
                        onPinCreated = { pin ->
                            navController.navigate("auth/confirm_pin/$pin")
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable(
                    route = "auth/confirm_pin/{originalPin}",
                    arguments = listOf(navArgument("originalPin") { type = NavType.StringType })
                ) { backStackEntry ->
                    val originalPin = backStackEntry.arguments?.getString("originalPin") ?: ""
                    
                    ImprovedConfirmPinScreen(
                        originalPin = originalPin,
                        onPinConfirmed = {
                            navController.navigate(Graph.MAIN) {
                                popUpTo(Graph.AUTH) { inclusive = true }
                            }
                        },
                        onPinMismatch = {
                            // Pin mismatch handled in the screen itself
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            // --- MAIN APP GRAPH (now part of the single NavHost) ---
            navigation(
                startDestination = BottomNavItem.HOME.route,
                route = Graph.MAIN
            ) {
                // Bottom Bar Screens
                composable(BottomNavItem.HOME.route) {
                    WalletScreen(
                        onNavigateToAccounts = { navController.navigate("accounts") },
                        onNavigateToAccountDetails = { accountId -> navController.navigate("account_details/$accountId") },
                        //TODO: Remove these once WalletScreen is updated
                        onNavigateToTransactionHistory = {},
                        userId = ""
                    )
                }
                composable(BottomNavItem.LEARN.route) { 
                    LearnScreen(
                        onNavigateToLearningPath = { categoryId ->
                            navController.navigate("learning_path/$categoryId")
                        }
                    )
                }
                composable(BottomNavItem.CHAT.route) { ChatScreen() }
                composable(BottomNavItem.PROFILE.route) {
                    ActualProfileScreen(
                        onLogout = {
                            navController.navigate(Graph.AUTH) {
                                popUpTo(Graph.MAIN) { inclusive = true }
                            }
                        }
                    )
                }

                // Detail Screens (without bottom bar)
                composable("accounts") {
                    AccountsScreen(
                        onNavigateToTransactions = { accountId ->
                            navController.navigate("account_transactions/$accountId")
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "account_details/{accountId}",
                    arguments = listOf(navArgument("accountId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
                    AccountDetailsScreen(
                        accountId = accountId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToTransactions = {
                            navController.navigate("account_transactions/$accountId")
                        }
                    )
                }
                composable(
                    route = "account_transactions/{accountId}",
                    arguments = listOf(navArgument("accountId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val accountId = backStackEntry.arguments?.getString("accountId")!!
                    AccountTransactionsScreen(
                        accountId = accountId,
                        onNavigateBack = { navController.popBackStack() },
                        onTransactionClick = { transactionId ->
                            // TODO: navController.navigate("transaction_details/$transactionId")
                        }
                    )
                }
                
                composable(
                    route = "learning_path/{categoryId}",
                    arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                    LearningPathScreen(
                        categoryId = categoryId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

// This composable builds the bottom bar.
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val screens = listOf(
        BottomNavItem.HOME,
        BottomNavItem.LEARN,
        BottomNavItem.CHAT,
        BottomNavItem.PROFILE
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        screens.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            NavigationBarItem(
                label = { Text(screen.label) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.label
                    )
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}


// --- REMOVE MainAppScaffold() ENTIRELY ---
// It is no longer needed as its logic is now in AppNavigation()

// Generic placeholder screen
@Composable
fun GenericScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
    }
}

// Graph routes object
object Graph {
    const val AUTH = "auth_graph"
    const val MAIN = "main_graph"
}
