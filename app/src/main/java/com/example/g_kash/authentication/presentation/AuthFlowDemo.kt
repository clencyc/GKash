package com.example.g_kash.authentication.presentation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * Demo composable to test the new auth flow screens
 * You can use this for testing the screens individually
 */
@Composable
fun AuthFlowDemo(
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(AuthDemoScreen.CREATE_ACCOUNT) }
    var userData by remember { mutableStateOf(UserData()) }
    var pin by remember { mutableStateOf("") }
    
    when (currentScreen) {
        AuthDemoScreen.CREATE_ACCOUNT -> {
            ImprovedCreateAccountScreen(
                onNavigateToPin = { name, phone, idNumber ->
                    userData = userData.copy(name = name, phone = phone, idNumber = idNumber)
                    currentScreen = AuthDemoScreen.CREATE_PIN
                },
                onNavigateToLogin = {
                    // Navigate to login
                },
                onNavigateBack = {
                    // Navigate back
                },
                modifier = modifier
            )
        }
        
        AuthDemoScreen.CREATE_PIN -> {
            ImprovedCreatePinScreen(
                userName = userData.name,
                onPinCreated = { pinCode ->
                    pin = pinCode
                    currentScreen = AuthDemoScreen.CONFIRM_PIN
                },
                onNavigateBack = {
                    currentScreen = AuthDemoScreen.CREATE_ACCOUNT
                },
                modifier = modifier
            )
        }
        
        AuthDemoScreen.CONFIRM_PIN -> {
            ImprovedConfirmPinScreen(
                originalPin = pin,
                onPinConfirmed = {
                    // Account creation complete!
                    // Navigate to main app
                },
                onPinMismatch = {
                    // Pin mismatch handled in screen
                },
                onNavigateBack = {
                    currentScreen = AuthDemoScreen.CREATE_PIN
                },
                modifier = modifier
            )
        }
    }
}

private data class UserData(
    val name: String = "",
    val phone: String = "",
    val idNumber: String = ""
)

private enum class AuthDemoScreen {
    CREATE_ACCOUNT,
    CREATE_PIN,
    CONFIRM_PIN
}

/**
 * Individual screen testing composables
 */
@Composable
fun TestCreateAccountScreen(modifier: Modifier = Modifier) {
    ImprovedCreateAccountScreen(
        onNavigateToPin = { name, phone, idNumber ->
            println("Account created: $name, $phone, $idNumber")
        },
        onNavigateToLogin = {
            println("Navigate to login")
        },
        onNavigateBack = {
            println("Navigate back")
        },
        modifier = modifier
    )
}

@Composable
fun TestCreatePinScreen(modifier: Modifier = Modifier) {
    ImprovedCreatePinScreen(
        userName = "John Doe",
        onPinCreated = { pin ->
            println("Pin created: $pin")
        },
        onNavigateBack = {
            println("Navigate back")
        },
        modifier = modifier
    )
}

@Composable
fun TestConfirmPinScreen(modifier: Modifier = Modifier) {
    ImprovedConfirmPinScreen(
        originalPin = "1234",
        onPinConfirmed = {
            println("Pin confirmed!")
        },
        onPinMismatch = {
            println("Pin mismatch")
        },
        onNavigateBack = {
            println("Navigate back")
        },
        modifier = modifier
    )
}