package com.example.g_kash.authentication.presentation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.g_kash.authentication.data.ExtractedIdData
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

enum class KycStep {
    WELCOME,
    UPLOAD_ID,
    ADD_PHONE,
    VERIFY_PHONE,
    CREATE_PIN,
    CONFIRM_PIN,
    COMPLETE
}

/**
 * Main KYC Flow Screen that orchestrates the entire KYC process
 * This screen manages navigation between different KYC steps and handles the overall flow
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycFlowScreen(
    onKycComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    kycViewModel: KycViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by kycViewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Observe KYC events
    LaunchedEffect(kycViewModel) {
        kycViewModel.events.collect { event ->
            when (event) {
                is KycEvent.NavigateToNext -> {
                    // Let the ViewModel handle the step transition
                    // The ViewModel already updates the currentStep in the state
                    Log.d("KYC_UI", "NavigateToNext event received")
                }
                is KycEvent.NavigateBack -> {
                    onNavigateBack()
                }
                is KycEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Long
                    )
                }
                is KycEvent.ShowSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is KycEvent.RegistrationComplete -> {
                    // ViewModel already handles proper timing and token validation
                    onKycComplete()
                }
                is KycEvent.NavigateToLogin -> {
                    onKycComplete()
                }
            }
        }
    }
    
    // Debug log current step
    LaunchedEffect(uiState.currentStep) {
        Log.d("KYC_UI", "Current step in UI: ${uiState.currentStep}")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        when (uiState.currentStep) {
            KycStep.WELCOME -> {
                OnboardingScreen(
                    onGetStarted = {
                        kycViewModel.startKyc()
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            KycStep.UPLOAD_ID -> {
                KycUploadIdScreen(
                    onIdUploaded = { idUri, selfieUri ->
                        kycViewModel.uploadIdAndSelfie(context, idUri, selfieUri)
                    },
                    onNavigateBack = onNavigateBack,
                    isLoading = uiState.isLoading,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            KycStep.ADD_PHONE -> {
                KycAddPhoneScreen(
                    onPhoneAdded = { phoneNumber: String ->
                        kycViewModel.addPhoneNumber(phoneNumber)
                    },
                    onNavigateBack = {
                        kycViewModel.goBack()
                    },
                    isLoading = uiState.isLoading,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            KycStep.VERIFY_PHONE -> {
                KycVerifyPhoneScreen(
                    phoneNumber = uiState.phoneNumber,
                    onOtpVerified = { otp: String ->
                        kycViewModel.verifyOtp(otp)
                    },
                    onNavigateBack = {
                        kycViewModel.goBack()
                    },
                    onResendCode = {
                        kycViewModel.addPhoneNumber(uiState.phoneNumber)
                    },
                    isLoading = uiState.isLoading,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            KycStep.CREATE_PIN -> {
                KycCreatePinScreen(
                    onPinCreated = { pin ->
                        kycViewModel.createPin(pin)
                    },
                    onNavigateBack = {
                        kycViewModel.goBack()
                    },
                    isLoading = uiState.isLoading,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            KycStep.CONFIRM_PIN -> {
                ImprovedConfirmPinScreen(
                    originalPin = uiState.pin,
                    onPinConfirmed = {
                        kycViewModel.confirmPin(uiState.pin)
                    },
                    onPinMismatch = {
                        scope.launch {
                            snackbarHostState.showSnackbar("PINs do not match. Please try again.")
                        }
                    },
                    onNavigateBack = {
                        kycViewModel.goBack()
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            KycStep.COMPLETE -> {
                KycCompletionScreen(
                    extractedData = uiState.extractedData,
                    onContinueToLogin = {
                        // In demo mode, just complete the KYC flow
                        onKycComplete()
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

/**
 * KYC Completion Screen - Shows success and allows user to proceed to login
 */
@Composable
fun KycCompletionScreen(
    extractedData: ExtractedIdData?,
    onContinueToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success icon
        Card(
            modifier = Modifier.size(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E8)
            ),
            shape = androidx.compose.foundation.shape.CircleShape
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF4CAF50)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Registration Complete!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        extractedData?.let { data ->
            Text(
                text = "Welcome, ${data.user_name}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "ID: ${data.user_nationalId}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Your account has been created successfully!\nYou can now log in with your National ID and PIN.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onContinueToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6366F1),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Continue to Login",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}







/**
 * Enhanced Login Screen specifically for KYC users using National ID
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycLoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToSignup: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = koinViewModel()
) {
    var nationalId by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var showPinEntry by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF5F7FA),
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (!showPinEntry) {
                // National ID Entry
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Welcome Back",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Enter your National ID to continue",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        OutlinedTextField(
                            value = nationalId,
                            onValueChange = { 
                                nationalId = it.filter { char -> char.isDigit() }
                                loginError = null
                            },
                            label = { Text("National ID") },
                            placeholder = { Text("12345678") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            isError = loginError != null,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            )
                        )
                        
                        loginError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                if (nationalId.length >= 8) {
                                    showPinEntry = true
                                } else {
                                    loginError = "Please enter a valid National ID"
                                    scope.launch {
                                        snackbarHostState.showSnackbar("National ID is required")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            enabled = nationalId.length >= 8,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1),
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Continue",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                // PIN Entry
                ImprovedLoginPinEntry(
                    userIdNumber = nationalId,
                    onPinEntered = { enteredPin ->
                        // pin = enteredPin // This line is not needed as we are not using the pin variable
                        isLoading = true
                        // Here you would call the login API
                        // For now, simulate success after a delay
                        scope.launch {
                            kotlinx.coroutines.delay(1500)
                            isLoading = false
                            onLoginSuccess()
                        }
                    },
                    onBackClick = { showPinEntry = false },
                    showError = loginError != null,
                    isLoading = isLoading
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            TextButton(onClick = onNavigateToSignup) {
                Text("Don't have an account? Sign Up")
            }
        }
    }
}