package com.example.g_kash.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.g_kash.authentication.presentation.AuthViewModel
import com.example.g_kash.authentication.presentation.ConfirmPinScreen
import com.example.g_kash.authentication.presentation.CoursesScreen
import com.example.g_kash.authentication.presentation.CreateAccountScreen
import com.example.g_kash.authentication.presentation.CreatePinScreen
import com.example.g_kash.authentication.presentation.EditProfileScreen
import com.example.g_kash.authentication.presentation.LoginScreen
// Important: Make sure your Destination object/sealed class is imported correctly
import com.example.g_kash.authentication.presentation.Navigation.Destination
import com.example.g_kash.authentication.presentation.ReceiveMoneyScreen
import com.example.g_kash.authentication.presentation.SendMoneyScreen
import com.example.g_kash.authentication.presentation.SettingsScreen
import com.example.g_kash.authentication.presentation.TransactionHistoryScreen
// Import MainAppScaffold if it's defined elsewhere
// import com.example.g_kash.ui.MainAppScaffold
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(
    authViewModel: AuthViewModel = koinViewModel() // Inject the ViewModel
) {
    val navController = rememberNavController()

    // --- CORRECT WAY TO GET AUTH STATE ---
    // Collect the AuthState from the ViewModel.
    // The ViewModel MUST provide a StateFlow<AuthState> with an initial value.
    val authState by authViewModel.authState.collectAsState()
    // -------------------------------------

    // Determine the start destination based on the collected authentication state
    val startDestination = if (authState.isAuthenticated) {
        // If authenticated, navigate to your main app entry point (e.g., scaffold with bottom nav)
        // Use a consistent route for your main app scaffold.
        "main_app_entry_route"
    } else {
        // If not authenticated, start with the authentication flow
        Destination.CreateAccount.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        // ==================== AUTH FLOW ====================
        composable(Destination.CreateAccount.route) {
            CreateAccountScreen(
                onNavigateToLogin = { navController.navigate(Destination.Login.route) },
                onAccountCreated = { userId ->
                    navController.navigate(Destination.CreatePin.createRoute(userId)) {
                        popUpTo(Destination.CreateAccount.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Destination.CreatePin.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId").orEmpty() // Use .orEmpty() for safety
            CreatePinScreen(
                userId = userId,
                onPinCreated = { pin ->
                    navController.navigate(Destination.ConfirmPin.createRoute(userId, pin))
                }
            )
        }

        composable(Destination.ConfirmPin.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId").orEmpty()
            val pin = backStackEntry.arguments?.getString("pin").orEmpty()
            ConfirmPinScreen(
                userId = userId,
                expectedPin = pin,
                onPinConfirmed = {
                    // After confirmation, navigate to the main app entry point
                    navController.navigate("main_app_entry_route") { // Ensure this matches the startDestination if authenticated
                        popUpTo(Destination.CreateAccount.route) { inclusive = true } // Clear the auth flow
                    }
                }
            )
        }

        composable(Destination.Login.route) {
            LoginScreen(
                onNavigateToSignup = { navController.navigate(Destination.CreateAccount.route) },
                onLoginSuccess = {
                    // After successful login, navigate to the main app entry point.
                    // The ViewModel should have updated authState, which will cause startDestination to re-evaluate.
                    // However, for immediate navigation, explicitly navigate here.
                    navController.navigate("main_app_entry_route") { // Navigate to main app entry
                        popUpTo(Destination.Login.route) { inclusive = true } // Clear login screen
                    }
                }
            )
        }

        // ==================== MAIN APP ENTRY POINT ====================
        // This route leads to your main app structure (likely Scaffold with BottomNav)
        composable("main_app_entry_route") {
            // Ensure MainAppScaffold is defined and imported correctly.
            // It should likely receive the navController and potentially the authViewModel
            // if its nested screens need them.
            MainAppScaffold(
                rootNavController = navController, // Pass navController if needed by nested NavHost
                authViewModel = authViewModel // Pass ViewModel if needed
            )
        }

        // ==================== Other Screens (Transactions, Learning, Profile) ====================
        // IMPORTANT: These screens (SendMoney, ReceiveMoney, TransactionHistory, Courses, Settings, EditProfile)
        // are typically part of the "main_app_entry_route" structure, likely within the NavHost
        // inside your MainAppScaffold. They should NOT typically be defined directly here
        // unless they are accessible BEFORE full authentication or outside the bottom nav.
        // If they are part of the bottom nav, move their composable definitions into
        // the NavHost within MainAppScaffold.

        composable(Destination.SendMoney.route) {
            SendMoneyScreen(
                onNavigateBack = { navController.popBackStack() },
                onTransactionComplete = { navController.popBackStack() }
            )
        }
        composable(Destination.ReceiveMoney.route) {
            ReceiveMoneyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Destination.TransactionHistory.route) {
            TransactionHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onTransactionClick = { transactionId ->
                    // Assuming TransactionDetails is a defined route in Destination
                    navController.navigate(Destination.TransactionDetails.createRoute(transactionId))
                }
            )
        }
        composable(Destination.Courses.route) {
            CoursesScreen(
                onNavigateBack = { navController.popBackStack() },
                onCourseClick = { courseId ->
                    // Assuming CourseDetails is a defined route in Destination
                    navController.navigate(Destination.CourseDetails.createRoute(courseId))
                }
            )
        }
        composable(Destination.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Destination.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}