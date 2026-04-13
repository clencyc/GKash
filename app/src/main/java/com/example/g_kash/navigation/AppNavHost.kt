package com.example.g_kash.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
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
import com.example.g_kash.goals.presentation.GoalsScreen
import com.example.g_kash.groups.presentation.GroupsScreen
import com.example.g_kash.investment.presentation.InvestmentSimulatorScreen
import com.example.g_kash.investment.presentation.InvestmentAccountCreationScreen
import com.example.g_kash.investment.presentation.InvestmentScreen
import com.example.g_kash.investment.presentation.ReceiptScreen
import com.example.g_kash.leaderboard.presentation.LeaderboardScreen
import com.example.g_kash.points.presentation.PointsStoreScreen
import com.example.g_kash.points.presentation.EnhancedProfileScreen
import com.example.g_kash.transactions.presentation.TransactionsScreen
import com.example.g_kash.payment.presentation.DepositScreen
import com.example.g_kash.payment.presentation.PaymentReceiptScreen
import com.example.g_kash.payment.data.PaymentReceipt
import com.example.g_kash.budget.presentation.BudgetSimulatorScreen
import org.koin.androidx.compose.koinViewModel

// Main navigation setup for the entire application
@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = koinViewModel()
    val uiAuthState by authViewModel.uiAuthState.collectAsState()
    val navController = rememberNavController()

    // Screens that should show the bottom bar.
    val bottomBarScreens = listOf(
        "main/home",
        "main/learn",
        "main/investment_simulator",
        "main/chat",
        "main/profile",
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val shouldShowBottomBar = currentRoute in bottomBarScreens

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                CustomBottomNavBar(navController = navController)
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
                startDestination = "auth/onboarding",
                route = Graph.AUTH
            ) {
                // Onboarding screen - first thing unauthenticated users see
                composable("auth/onboarding") {
                    OnboardingScreen(
                        onNavigateToLogin = {
                            navController.navigate("auth/login") {
                                popUpTo("auth/onboarding") { inclusive = true }
                            }
                        },
                        onNavigateToRegister = {
                            navController.navigate("auth/kyc") {
                                popUpTo("auth/onboarding") { inclusive = true }
                            }
                        }
                    )
                }
                
                // Login screen - for existing users
                composable("auth/login") {
                    val loginState by authViewModel.loginState.collectAsState()

                    // React to login success
                    LaunchedEffect(loginState) {
                        if (loginState is LoginState.Success) {
                            navController.navigate(Graph.MAIN) {
                                popUpTo(Graph.AUTH) { inclusive = true }
                            }
                            authViewModel.resetLoginState()
                        }
                    }

                    ImprovedLoginEmailPinScreen(
                        isLoading = loginState is LoginState.Loading,
                        showError = (loginState as? LoginState.Error)?.message,
                        onLoginSuccess = { email, pin ->
                            authViewModel.login(email, pin)
                        },
                        onNavigateBack = {
                            authViewModel.resetLoginState()
                            navController.navigate("auth/onboarding") {
                                popUpTo("auth/login") { inclusive = true }
                            }
                        }
                    )
                }


                
                // KYC Flow (Registration Process)
                composable("auth/kyc") {
                    KycFlowScreen(
                        onKycComplete = { token ->
                            // After KYC with registration complete, go directly to main app with token
                            // Token is already saved in SessionStorage by ViewModel
                            navController.navigate(Graph.MAIN) {
                                popUpTo(Graph.AUTH) { inclusive = true }
                            }
                        },
                        onNavigateBack = {
                            navController.navigate("auth/onboarding") {
                                popUpTo("auth/kyc") { inclusive = true }
                            }
                        }
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
                startDestination = "main/home",
                route = Graph.MAIN
            ) {
                // Bottom Bar Screens
                composable("main/home") {
                    WalletScreen(
                        onNavigateToAccounts = { navController.navigate("accounts") },
                        onNavigateToAccountDetails = { accountId -> navController.navigate("account_details/$accountId") },
                        onNavigateToTransactionHistory = { navController.navigate("main/transactions") },
                        onNavigateToInvestment = { navController.navigate(Destination.Deposit.createRoute("")) },
                        onNavigateToBudgetSimulator = { navController.navigate(Destination.BudgetSimulator.route) },
                        userId = ""
                    )
                }
                
                composable("main/learn") {
                    LearnScreen(
                        onNavigateToLearningPath = { categoryId ->
                            navController.navigate("learning_path/$categoryId")
                        }
                    )
                }
                
                composable("main/investment_simulator") {
                    InvestmentSimulatorScreen()
                }
                
                composable("main/chat") {
                    ChatScreen()
                }
                
                composable("main/profile") {
                    EnhancedProfileScreen(
                        onLogout = {
                            navController.navigate(Graph.AUTH) {
                                popUpTo(Graph.MAIN) { inclusive = true }
                            }
                        },
                        onNavigateToPointsStore = {
                            navController.navigate("main/points_store")
                        }
                    )
                }

                composable("main/transactions") {
                    TransactionsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                // Additional screens - accessible but not in main bottom bar
                composable("main/goals") {
                    GoalsScreen()
                }
                
                composable("main/groups") {
                    GroupsScreen()
                }
                
                composable("main/leaderboard") {
                    LeaderboardScreen()
                }
                
                composable("main/points_store") {
                    PointsStoreScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Destination.BudgetSimulator.route) {
                    BudgetSimulatorScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onInvestClick = { amount ->
                            // Agentic Bridge: Direct the user to the investment flow with pre-filled context
                            navController.navigate(Destination.Deposit.createRoute(""))
                        }
                    )
                }

                // Detail Screens (without bottom bar)
                composable("accounts") {
                    AccountsScreen(
                        onNavigateToTransactions = { accountId ->
                            navController.navigate("account_transactions/$accountId")
                        },
                        onNavigateToDeposit = { accountId ->
                            navController.navigate(Destination.Deposit.createRoute(accountId))
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
                        },
                        onNavigateToDeposit = {
                            navController.navigate(Destination.Deposit.createRoute(accountId))
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
                
                composable("investment_account_creation") {
                    InvestmentAccountCreationScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onAccountCreated = {
                            navController.navigate("main/home") {
                                popUpTo("investment_account_creation") { inclusive = true }
                            }
                        }
                    )
                }

                composable("investment") {
                    DepositScreen(
                        preselectedAccountId = "",
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToReceipt = { receipt ->
                            navController.navigate(Destination.PaymentReceipt.createRoute(receipt)) {
                                popUpTo("main/home") { inclusive = false }
                            }
                        }
                    )
                }

                composable(
                    route = "investment_receipt/{reference}/{amount}/{timestamp}/{type}/{status}",
                    arguments = listOf(
                        navArgument("reference") { type = NavType.StringType },
                        navArgument("amount") { type = NavType.StringType },
                        navArgument("timestamp") { type = NavType.StringType },
                        navArgument("type") { type = NavType.StringType },
                        navArgument("status") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val reference = backStackEntry.arguments?.getString("reference") ?: ""
                    val amount = backStackEntry.arguments?.getString("amount")?.toDoubleOrNull() ?: 0.0
                    val timestamp = backStackEntry.arguments?.getString("timestamp") ?: ""
                    val type = backStackEntry.arguments?.getString("type") ?: "GKash Savings"
                    val status = backStackEntry.arguments?.getString("status") ?: "Completed"

                    ReceiptScreen(
                        receipt = com.example.g_kash.investment.data.InvestmentReceipt(
                            transactionReference = reference,
                            amount = amount,
                            investmentType = type,
                            timestamp = timestamp,
                            status = status
                        ),
                        onDone = {
                            navController.navigate("main/home") {
                                popUpTo("main/home") { inclusive = true }
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Payment / Deposit screens
                composable(
                    route = Destination.Deposit.route,
                    arguments = listOf(
                        navArgument("accountId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = ""
                        }
                    )
                ) { backStackEntry ->
                    val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
                    DepositScreen(
                        preselectedAccountId = accountId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToReceipt = { receipt ->
                            navController.navigate(
                                "payment/receipt/" +
                                    "${Uri.encode(receipt.transactionReference)}/" +
                                    "${Uri.encode(receipt.amount.toString())}/" +
                                    "${Uri.encode(receipt.accountType)}/" +
                                    "${Uri.encode(receipt.accountId)}/" +
                                    "${Uri.encode(receipt.timestamp)}/" +
                                    Uri.encode(receipt.phone)
                            ) {
                                // Pop the deposit entry from backstack
                                popUpTo(Destination.Deposit.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route = "payment/receipt/{reference}/{amount}/{accountType}/{accountId}/{timestamp}/{phone}",
                    arguments = listOf(
                        navArgument("reference")   { type = NavType.StringType },
                        navArgument("amount")      { type = NavType.StringType },
                        navArgument("accountType") { type = NavType.StringType },
                        navArgument("accountId")   { type = NavType.StringType },
                        navArgument("timestamp")   { type = NavType.StringType },
                        navArgument("phone")       { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val reference   = Uri.decode(backStackEntry.arguments?.getString("reference")   ?: "")
                    val amount      = Uri.decode(backStackEntry.arguments?.getString("amount")      ?: "0").toDoubleOrNull() ?: 0.0
                    val accountType = Uri.decode(backStackEntry.arguments?.getString("accountType") ?: "")
                    val accountId   = Uri.decode(backStackEntry.arguments?.getString("accountId")   ?: "")
                    val timestamp   = Uri.decode(backStackEntry.arguments?.getString("timestamp")   ?: "")
                    val phone       = Uri.decode(backStackEntry.arguments?.getString("phone")       ?: "")

                    PaymentReceiptScreen(
                        receipt = PaymentReceipt(
                            transactionReference = reference,
                            amount = amount,
                            accountType = accountType,
                            accountId = accountId,
                            timestamp = timestamp,
                            phone = phone
                        ),
                        onDone = {
                            navController.navigate("main/home") {
                                popUpTo("main/home") { inclusive = true }
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
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
