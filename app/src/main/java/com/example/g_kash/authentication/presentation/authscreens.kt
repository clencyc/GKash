package com.example.g_kash.authentication.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
fun CreateAccountScreen(
    onNavigateToLogin: () -> Unit,
    onAccountCreated: (userId: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {newName -> name = newName},
            label = { Text("Name")
            })
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { newPhoneNumber -> phoneNumber = newPhoneNumber },
            label = { Text("Phone Number")
            })
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = idNumber,
            onValueChange = { newIdNumber -> idNumber = newIdNumber },
            label = { Text("ID Number")
            })
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onAccountCreated("user123") }) {
            Text("Continue")
        }
        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Log In")
        }
    }
}



@Composable
fun CreatePinScreen(
    userId: String,
    onPinCreated: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }

    PinInputScreen(
        title = "Create Pin",
        subtitle = null,
        pin = pin,
        onPinChange = { newPin -> pin = newPin },
        onActionClick = { onPinCreated(pin) },
        actionButtonText = "Next",
        modifier = modifier
    )
}

@Composable
fun ConfirmPinScreen(
    userId: String,
    expectedPin: String,
    onPinConfirmed: () -> Unit,
    onPinMismatch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(pin) {
        if (pin.length == 4) {
            if (pin == expectedPin) {
                onPinConfirmed()
            } else {
                showError = true
                kotlinx.coroutines.delay(500)
                pin = ""
                showError = false
                onPinMismatch()
            }
        }
    }

    PinInputScreen(
        title = "Confirm Pin",
        subtitle = "Re-enter your PIN to confirm",
        pin = pin,
        onPinChange = { newPin -> pin = newPin },
        onActionClick = { /* Auto-validates */ },
        actionButtonText = "Continue to App",
        showError = showError,
        modifier = modifier
    )
}

@Composable
private fun PinInputScreen(
    title: String,
    subtitle: String?,
    pin: String,
    onPinChange: (String) -> Unit,
    onActionClick: () -> Unit,
    actionButtonText: String,
    showError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val maxPinLength = 4

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        // Logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFBBF24), // yellow-400
                            Color(0xFFD97706)  // yellow-600
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "G",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Learn. Invest. Grow",
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        subtitle?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // PIN Dots Display
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 24.dp)
        ) {
            repeat(maxPinLength) { index ->
                PinDot(
                    isFilled = index < pin.length,
                    isError = showError
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Number Pad
        NumberPad(
            onNumberClick = { number ->
                if (pin.length < maxPinLength) {
                    onPinChange(pin + number)
                }
            },
            onBackspaceClick = {
                if (pin.isNotEmpty()) {
                    onPinChange(pin.dropLast(1))
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Button
        Button(
            onClick = onActionClick,
            enabled = pin.length == maxPinLength,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF111827), // gray-900
                disabledContainerColor = Color(0xFF9CA3AF) // gray-400
            )
        ) {
            Text(
                text = actionButtonText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PinDot(
    isFilled: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isFilled) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "pin_dot_scale"
    )

    Box(
        modifier = modifier
            .size(16.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                when {
                    isError -> Color(0xFFEF4444) // red-500
                    isFilled -> Color(0xFF111827) // gray-900
                    else -> Color(0xFFD1D5DB) // gray-300
                }
            )
    )
}

@Composable
private fun NumberPad(
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Rows 1-3 (numbers 1-9)
        for (row in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (col in 1..3) {
                    val number = (row * 3 + col).toString()
                    NumberButton(
                        text = number,
                        onClick = { onNumberClick(number) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Last row (0 and backspace)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            NumberButton(
                text = "0",
                onClick = { onNumberClick("0") },
                modifier = Modifier.weight(1f)
            )

            NumberButton(
                text = "",
                onClick = onBackspaceClick,
                isBackspace = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NumberButton(
    text: String,
    onClick: () -> Unit,
    isBackspace: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    Button(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier
            .aspectRatio(1f)
            .scale(if (isPressed) 0.95f else 1f),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF3F4F6), // gray-100
            contentColor = Color(0xFF111827) // gray-900
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        if (isBackspace) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Backspace",
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}
@Composable
fun LoginScreen(onNavigateToSignup: () -> Unit, onLoginSuccess: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome Back", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = "", onValueChange = {}, label = { Text("KM Number") })
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onLoginSuccess) {
            Text("Enter Pin")
        }
        TextButton(onClick = onNavigateToSignup) {
            Text("Don't have an account? Sign Up")
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateToSendMoney: () -> Unit,
    onNavigateToReceiveMoney: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Home", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Wallet Cash", fontSize = 14.sp)
                Text("1,100.00 ₴", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onNavigateToSendMoney, modifier = Modifier.weight(1f)) {
                Text("Send")
            }
            Button(onClick = onNavigateToReceiveMoney, modifier = Modifier.weight(1f)) {
                Text("Receive")
            }
        }
    }
}

@Composable
fun WalletScreen(onNavigateToTransactionHistory: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Wallet", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToTransactionHistory) {
            Text("View Transaction History")
        }
    }
}

@Composable
fun LearnScreen(onNavigateToCourses: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Learn & Grow Your Savings", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToCourses) {
            Text("Browse Courses")
        }
    }
}

@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToEditProfile, modifier = Modifier.fillMaxWidth()) {
            Text("Edit Profile")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onNavigateToSettings, modifier = Modifier.fillMaxWidth()) {
            Text("Settings")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text("Logout")
        }
    }
}

@Composable
fun SendMoneyScreen(onNavigateBack: () -> Unit, onTransactionComplete: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Send Money", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ReceiveMoneyScreen(onNavigateBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Receive Money", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TransactionHistoryScreen(
    onNavigateBack: () -> Unit,
    onTransactionClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Transaction History", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CoursesScreen(onNavigateBack: () -> Unit, onCourseClick: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Courses", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EditProfileScreen(onNavigateBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Edit Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}