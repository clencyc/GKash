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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

enum class LoginStep {
    ID_NUMBER,
    PIN_ENTRY
}

@Composable
fun CreateAccountScreen(
    onNavigateToLogin: () -> Unit,
    onAccountCreated: (userId: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Back Button and Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to previous screen",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "G-Kash",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(48.dp)) // Balance the layout
            }

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                isError = name.isBlank() && errorMessage != null
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    if (it.length <= 10) phoneNumber = it.filter { char -> char.isDigit() }
                },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                isError = phoneNumber.length < 10 && errorMessage != null,
                supportingText = { if (phoneNumber.length < 10 && errorMessage != null) Text("Enter a valid 10-digit phone number") }
            )

            OutlinedTextField(
                value = idNumber,
                onValueChange = { idNumber = it.filter { char -> char.isDigit() } },
                label = { Text("ID Number") },
                modifier = Modifier.fillMaxWidth(),
                isError = idNumber.isBlank() && errorMessage != null
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(
                onClick = {
                    when {
                        name.isBlank() -> errorMessage = "Name is required"
                        phoneNumber.length < 10 -> errorMessage = "Invalid phone number"
                        idNumber.isBlank() -> errorMessage = "ID Number is required"
                        else -> {
                            errorMessage = null
                            onAccountCreated("user_${idNumber}")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = name.isNotBlank() && phoneNumber.length >= 10 && idNumber.isNotBlank()
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = "Already have an account? Log In",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


@Composable
fun CreatePinScreen(
    userId: String,
    onPinCreated: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { padding ->
        PinInputScreen(
            title = "Create Your PIN",
            subtitle = "Create a 4-digit PIN for secure access",
            pin = pin,
            onPinChange = { pin = it },
            onActionClick = {
                if (pin.length == 4) {
                    onPinCreated(pin)
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Please enter a 4-digit PIN")
                    }
                }
            },
            actionButtonText = "Next",
            onBackClick = onNavigateBack,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}


@Composable
fun ConfirmPinScreen(
    userId: String,
    expectedPin: String,
    onPinConfirmed: () -> Unit,
    onPinMismatch: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pin) {
        if (pin.length == 4) {
            if (pin == expectedPin) {
                onPinConfirmed()
            } else {
                showError = true
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("PINs do not match. Please try again.")
                }
                pin = ""
                onPinMismatch()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { padding ->
        PinInputScreen(
            title = "Confirm Your PIN",
            subtitle = "Re-enter your 4-digit PIN to confirm",
            pin = pin,
            onPinChange = { pin = it },
            onActionClick = { /* Handled by LaunchedEffect */ },
            actionButtonText = "Continue to App",
            showError = showError,
            onBackClick = onNavigateBack,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}


@Composable
fun LoginScreen(
    onNavigateToSignup: () -> Unit,
    onNavigateToKyc: () -> Unit = {},
    onLoginSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = koinViewModel()
) {
    var idNumber by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var currentLoginStep by remember { mutableStateOf(LoginStep.ID_NUMBER) }
    var loginError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "G-Kash",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                AnimatedVisibility(
                    visible = currentLoginStep == LoginStep.ID_NUMBER,
                    enter = scaleIn(),
                    exit = scaleOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = idNumber,
                            onValueChange = {
                                idNumber = it
                                loginError = null
                            },
                            label = { Text("ID Number") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = loginError != null
                        )

                        Button(
                            onClick = {
                                if (idNumber.isNotBlank()) {
                                    currentLoginStep = LoginStep.PIN_ENTRY
                                } else {
                                    loginError = "Please enter your ID Number"
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("ID Number is required")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = idNumber.isNotBlank()
                        ) {
                            Text(
                                text = "Enter Pin",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = currentLoginStep == LoginStep.PIN_ENTRY,
                    enter = scaleIn(),
                    exit = scaleOut()
                ) {
                    PinInputScreen(
                        title = "Enter Your PIN",
                        subtitle = "Enter the PIN for ID: $idNumber",
                        pin = pin,
                        onPinChange = {
                            pin = it
                            loginError = null
                        },
                        onActionClick = {
                            if (pin.length == 4) {
                                // Replace with authViewModel.login(idNumber, pin)
                                onLoginSuccess()
                            } else {
                                loginError = "Invalid PIN"
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Invalid PIN. Please try again.")
                                }
                            }
                        },
                        actionButtonText = "Login",
                        showError = loginError != null,
                        onBackClick = { currentLoginStep = LoginStep.ID_NUMBER }
                    )
                }

                loginError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                TextButton(onClick = onNavigateToSignup) {
                    Text(
                        text = "New User? Create Account with KYC",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun PinInputScreen(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String?,
    pin: String,
    onPinChange: (String) -> Unit,
    onActionClick: () -> Unit,
    actionButtonText: String,
    showError: Boolean = false,
    onBackClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                onBackClick?.let {
                    IconButton(onClick = it) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } ?: Spacer(modifier = Modifier.width(48.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Learn. Invest. Grow",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // PIN Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp), // Reduced from 12.dp
            modifier = Modifier.padding(vertical = 12.dp) // Reduced from 16.dp
        ) {
            repeat(4) { index ->
                PinDot(
                    isFilled = index < pin.length,
                    isError = showError
                )
            }
        }

        // Number Pad
        NumberPad(
            onNumberClick = { number ->
                if (pin.length < 4) onPinChange(pin + number)
            },
            onBackspaceClick = {
                if (pin.isNotEmpty()) onPinChange(pin.dropLast(1))
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Allow number pad to take available space
        )

        // Action Button
        Button(
            onClick = onActionClick,
            enabled = pin.length == 4,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        ) {
            Text(
                text = actionButtonText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
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
            .size(10.dp) // Reduced from 12.dp
            .scale(scale)
            .clip(CircleShape)
            .background(
                when {
                    isError -> MaterialTheme.colorScheme.error
                    isFilled -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
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
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp) // Reduced from 8.dp
    ) {
        // Rows 1-3 (numbers 1-9)
        for (row in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp) // Reduced from 8.dp
            ) {
                for (col in 1..3) {
                    val number = (row * 3 + col).toString()
                    NumberButton(
                        text = number,
                        onClick = { onNumberClick(number) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp) // Reduced from 64.dp
                    )
                }
            }
        }

        // Last row (0 and backspace)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp) // Reduced from 8.dp
        ) {
            Spacer(modifier = Modifier.weight(1f))
            NumberButton(
                text = "0",
                onClick = { onNumberClick("0") },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp) // Reduced from 64.dp
            )
            NumberButton(
                text = "",
                onClick = onBackspaceClick,
                isBackspace = true,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp) // Reduced from 64.dp
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
        modifier = modifier.scale(if (isPressed) 0.95f else 1f),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
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
                modifier = Modifier.size(20.dp) // Reduced from 24.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall, // Changed to smaller typography
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSendMoney: () -> Unit,
    onNavigateToReceiveMoney: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("G-Kash", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Hello, User!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Wallet Cash",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "$1,100.00", // Replace with dynamic currency
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onNavigateToSendMoney,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Send", style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = onNavigateToReceiveMoney,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Receive", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
