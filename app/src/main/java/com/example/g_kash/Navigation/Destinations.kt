package com.example.g_kash.Navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

// Sealed class for type-safe navigation
sealed class Destination(val route: String) {
    // Auth destinations
    object CreateAccount : Destination("auth/create_account")
    object CreatePin : Destination("auth/create_pin/{userId}") {
        fun createRoute(userId: String) = "auth/create_pin/$userId"
    }
    object ConfirmPin : Destination("auth/confirm_pin/{userId}/{pin}") {
        fun createRoute(userId: String, pin: String) = "auth/confirm_pin/$userId/$pin"
    }
    object Login : Destination("auth/login")

    // Main app destinations
    object Home : Destination("main/home")
    object Wallet : Destination("main/wallet")
    object Learn : Destination("main/learn")
    object Profile : Destination("main/profile")

    // Account destinations - NEW
    object Accounts : Destination("accounts/list")
    object AccountDetails : Destination("accounts/details/{accountId}") {
        fun createRoute(accountId: String) = "accounts/details/$accountId"
    }
    object CreateNewAccount : Destination("accounts/create")
    object AccountTransactions : Destination("accounts/{accountId}/transactions") {
        fun createRoute(accountId: String) = "accounts/$accountId/transactions"
    }

    // Transaction destinations
    object SendMoney : Destination("transactions/send")
    object ReceiveMoney : Destination("transactions/receive")
    object TransactionHistory : Destination("transactions/history")
    object TransactionDetails : Destination("transactions/details/{transactionId}") {
        fun createRoute(transactionId: String) = "transactions/details/$transactionId"
    }

    // Investment destinations
    object InvestmentPortfolio : Destination("invest/portfolio")
    object InvestmentDetails : Destination("invest/details/{investmentId}") {
        fun createRoute(investmentId: String) = "invest/details/$investmentId"
    }
    object BuyInvestment : Destination("invest/buy")

//    // Learning destinations
//    object Courses : Destination("learn/courses")
//    object CourseDetails : Destination("learn/course/{courseId}") {
//        fun createRoute(courseId: String) = "learn/course/$courseId"
//    }
//    object Quiz : Destination("learn/quiz/{quizId}") {
//        fun createRoute(quizId: String) = "learn/quiz/$quizId"
//    }
//
//    // Profile destinations
//    object Settings : Destination("profile/settings")
//    object EditProfile : Destination("profile/edit")
//    object SecuritySettings : Destination("profile/security")
//    object HelpSupport : Destination("profile/help")
}

// Navigation groups for better organization
object NavGraphs {
    const val AUTH = "auth_graph"
    const val MAIN = "main_graph"
    const val ACCOUNTS = "accounts_graph"
    const val TRANSACTIONS = "transactions_graph"
    const val INVESTMENTS = "investments_graph"
    const val LEARNING = "learning_graph"
    const val PROFILE = "profile_graph"
}