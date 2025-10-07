package com.example.g_kash.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.g_kash.accounts.presentation.AccountDetailsScreen
import com.example.g_kash.accounts.presentation.AccountsScreen
import com.example.g_kash.accounts.presentation.AccountsViewModel
import com.example.g_kash.authentication.presentation.*
import com.example.g_kash.core.presentation.LearnScreen
import com.example.g_kash.core.presentation.ProfileScreen
import com.example.g_kash.transactions.data.AccountTransactionsScreen
import com.example.g_kash.wallet.presentation.WalletScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AppNavHost(
    authViewModel: AuthViewModel = koinViewModel()
) {
    val uiAuthState by authViewModel.uiAuthState.collectAsState()

    // A single NavController for the entire app.
    val navController = rememberNavController()

    // List of screens that should show the bottom bar.
    val bottomBarScreens = listOf(
        Destination.Home.route,
        Destination.Wallet.route,
        Destination.Learn.route,
        Destination.Profile.route,
    )

    // Observe the current back stack entry to get the current route.
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Decide whether to show the bottom bar based on the current route.
    val shouldShowBottomBar = currentRoute in bottomBarScreens

    // Determine the starting screen based on authentication state.
    val startDestination = when (uiAuthState) {
        is UiAuthState.Authenticated -> Destination.Home.route
        is UiAuthState.Unauthenticated -> Destination.CreateAccount.route
        is UiAuthState.Unknown -> null // Will show a loading screen
    }

    if (startDestination != null) {
        // The Scaffold is the root layout.
        Scaffold(
            bottomBar = {
                // Conditionally show the bottom navigation bar.
                if (shouldShowBottomBar) {
                    BottomNavigationBar(navController = navController)
                }
            }
        ) { innerPadding ->
            // The single NavHost for the entire app.
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // Apply padding from the Scaffold
            ) {
                // --- Authentication Flow ---
                composable(Destination.CreateAccount.route) {
                    CreateAccountScreen(
                        onNavigateToLogin = { navController.navigate(Destination.Login.route) },
                        onAccountCreated = { userId ->
                            navController.navigate(Destination.CreatePin.createRoute(userId)) {
                                popUpTo(Destination.CreateAccount.route) { inclusive = true }
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Destination.CreatePin.route) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId").orEmpty()
                    CreatePinScreen(
                        userId = userId,
                        onPinCreated = { pin ->
                            navController.navigate(Destination.ConfirmPin.createRoute(userId, pin))
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Destination.ConfirmPin.route) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId").orEmpty()
                    val pin = backStackEntry.arguments?.getString("pin").orEmpty()
                    ConfirmPinScreen(
                        userId = userId,
                        expectedPin = pin,
                        onPinConfirmed = {
                            navController.navigate(Destination.Home.route) {
                                popUpTo(Destination.CreateAccount.route) { inclusive = true }
                            }
                        },
                        onPinMismatch = { /* Handle mismatch if needed */ },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Destination.Login.route) {
                    LoginScreen(
                        onNavigateToSignup = { navController.navigate(Destination.CreateAccount.route) },
                        onLoginSuccess = {
                            navController.navigate(Destination.Home.route) {
                                popUpTo(Destination.Login.route) { inclusive = true }
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // --- Main App Screens (with bottom bar) ---
                composable(Destination.Home.route) {
                    HomeScreen(
                        onNavigateToSendMoney = { /* navController.navigate(...) */ },
                        onNavigateToReceiveMoney = { /* navController.navigate(...) */ }
                    )
                }
                composable(Destination.Wallet.route) {
                    val userViewModel: UserViewModel = koinViewModel()
                    val userId by userViewModel.userId.collectAsState()
                    WalletScreen(
                        onNavigateToTransactionHistory = { /* navController.navigate(...) */ },
                        onNavigateToAccounts = { navController.navigate(Destination.Accounts.route) },
                        onNavigateToAccountDetails = { accountId ->
                            navController.navigate(Destination.AccountDetails.createRoute(accountId))
                        },
                        userId = userId ?: ""
                    )
                }
                composable(Destination.Learn.route) { LearnScreen() }
                composable(Destination.Profile.route) { ProfileScreen() }

                // --- Detail Screens (without bottom bar) ---
                composable(Destination.Accounts.route) {
                    val userViewModel: UserViewModel = koinViewModel()
                    val userId by userViewModel.userId.collectAsState()
                    userId?.let { id ->
                        val viewModel: AccountsViewModel = koinViewModel(parameters = { parametersOf(id) })
                        AccountsScreen(
                            viewModel = viewModel,
                            onNavigateToTransactions = { accountId ->
                                navController.navigate(Destination.AccountTransactions.createRoute(accountId))
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
                composable(
                    route = Destination.AccountDetails.route,
                    arguments = listOf(navArgument("accountId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val accountId = backStackEntry.arguments?.getString("accountId") ?: return@composable
                    val userViewModel: UserViewModel = koinViewModel()
                    val userId by userViewModel.userId.collectAsState()
                    userId?.let { id ->
                        val viewModel: AccountsViewModel = koinViewModel(parameters = { parametersOf(id) })
                        AccountDetailsScreen(
                            accountId = accountId,
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToTransactions = {
                                navController.navigate(Destination.AccountTransactions.createRoute(accountId))
                            }
                        )
                    }
                }
                composable(
                    route = Destination.AccountTransactions.route,
                    arguments = listOf(navArgument("accountId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val accountId = backStackEntry.arguments?.getString("accountId") ?: return@composable
                    AccountTransactionsScreen(
                        accountId = accountId,
                        onNavigateBack = { navController.popBackStack() },
                        onTransactionClick = { transactionId ->
                            // Make sure Destination.TransactionDetails is defined
                            // navController.navigate(Destination.TransactionDetails.createRoute(transactionId))
                        }
                    )
                }
            }
        }
    } else {
        // Show a loading indicator while determining the authentication state.
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

// The MainAppScaffold composable is no longer needed and can be deleted.

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navItems = listOf(
        Destination.Home to Icons.Default.Home,
        Destination.Wallet to Icons.Default.Wallet,
        Destination.Learn to Icons.Default.Info,
        Destination.Profile to Icons.Default.Person
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        navItems.forEach { (screen, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = null) },
                label = { Text(screen.route.substringAfter('/')) }, // Display "home", "wallet", etc.
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large back stack.
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when re-selecting the same item
                        launchSingleTop = true
                        // Restore state when re-selecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}