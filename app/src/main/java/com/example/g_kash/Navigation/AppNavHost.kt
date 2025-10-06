package com.example.g_kash.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.g_kash.authentication.presentation.AuthViewModel
import com.example.g_kash.authentication.presentation.ConfirmPinScreen
import com.example.g_kash.authentication.presentation.CreateAccountScreen
import com.example.g_kash.authentication.presentation.CreatePinScreen
import com.example.g_kash.authentication.presentation.HomeScreen
import com.example.g_kash.authentication.presentation.LoginScreen
import com.example.g_kash.Navigation.Destination
import com.example.g_kash.accounts.presentation.AccountDetailsScreen
import com.example.g_kash.accounts.presentation.AccountsScreen
import com.example.g_kash.accounts.presentation.AccountsViewModel
import com.example.g_kash.transactions.data.AccountTransactionsScreen
import com.example.g_kash.wallet.presentation.WalletScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf


@Composable
fun AppNavHost(
    authViewModel: AuthViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()
    val startDestination = if (authState.isAuthenticated) {
        Destination.Home.route
    } else {
        Destination.CreateAccount.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        // Authentication Flow
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

        // Main App with Bottom Navigation
        composable(Destination.Home.route) {
            MainAppScaffold(
                rootNavController = navController,
                authViewModel = authViewModel
            )
        }


// ============================================
// ACCOUNTS SCREENS (NEW)
// ============================================
        composable(Destination.Accounts.route) {
            val userId = authState.user?.id ?: ""
            val viewModel: AccountsViewModel = koinViewModel(
                parameters = { parametersOf(userId) }
            )

            AccountsScreen(
                viewModel = viewModel,
                onNavigateToTransactions = { accountId ->
                    navController.navigate(
                        Destination.AccountTransactions.createRoute(accountId)
                    )
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Destination.AccountDetails.route,
            arguments = listOf(
                navArgument("accountId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: return@composable
            val userId = authState.user?.id ?: ""
            val viewModel: AccountsViewModel = koinViewModel(
                parameters = { parametersOf(userId) }
            )

            AccountDetailsScreen(
                accountId = accountId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTransactions = {
                    navController.navigate(
                        Destination.AccountTransactions.createRoute(accountId)
                    )
                }
            )
        }

        composable(
            route = Destination.AccountTransactions.route,
            arguments = listOf(
                navArgument("accountId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: return@composable

            AccountTransactionsScreen(
                accountId = accountId,
                onNavigateBack = { navController.popBackStack() },
                onTransactionClick = { transactionId ->
                    navController.navigate(
                        Destination.TransactionDetails.createRoute(transactionId)
                    )
                }
            )
        }
    }
}

@Composable
fun MainAppScaffold(
    rootNavController: NavController,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val navController = rememberNavController()
    val navItems = listOf(
        Destination.Home,
        Destination.Wallet,
        Destination.Learn,
        Destination.Profile
    )
    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEach { destination ->
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Info, contentDescription = destination.route) }, // Replace with actual icons
                        label = { Text(destination.route) },
                        selected = navController.currentDestination?.route == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable(Destination.Home.route) {
                HomeScreen(
                    onNavigateToSendMoney = { navController.navigate(Destination.SendMoney.route) },
                    onNavigateToReceiveMoney = { navController.navigate(Destination.ReceiveMoney.route) }
                )
            }
            composable(Destination.Wallet.route) {
                WalletScreen(
                    onNavigateToTransactionHistory = {
                        rootNavController.navigate(Destination.TransactionHistory.route)
                    },
                    onNavigateToAccounts = {
                        rootNavController.navigate(Destination.Accounts.route)
                    },
                    onNavigateToAccountDetails = { accountId ->
                        rootNavController.navigate(Destination.AccountDetails.createRoute(accountId))
                    },
                    userId = authState.user?.id ?: ""
                )
            }

        }
    }
}