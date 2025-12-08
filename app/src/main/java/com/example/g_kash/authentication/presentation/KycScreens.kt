package com.example.g_kash.authentication.presentation

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Pink color scheme
private val PrimaryPink = Color(0xFFD91A5B)
private val LightPink = Color(0xFFFFF0F5)
private val DarkPink = Color(0xFFC4164E)
private val AccentPink = Color(0xFFFF6B9D)
private val TextDark = Color(0xFF1A1A1A)
private val TextLight = Color(0xFF6B6B6B)
private val SuccessGreen = Color(0xFF10B981)
private val BackgroundGradientStart = Color.White
private val BackgroundGradientEnd = Color(0xFFFFF5F9)

// STEP 1: Upload ID + Selfie
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycUploadIdScreen(
    onIdUploaded: (Uri, Uri) -> Unit,
    onNavigateBack: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var idImageUri by remember { mutableStateOf<Uri?>(null) }
    var selfieUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePicker by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Floating animation
    val infiniteTransition = rememberInfiniteTransition()
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            when (showImagePicker) {
                "id" -> idImageUri = it
                "selfie" -> selfieUri = it
            }
            showImagePicker = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(BackgroundGradientStart, BackgroundGradientEnd)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress indicator
                ProgressHeader(
                    stepText = "Step 1 of 4",
                    progressText = "25%",
                    progress = 0.25f
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Floating icon with glow
                FloatingIcon(
                    icon = Icons.Default.Description,
                    offset = floatingOffset
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Title and subtitle
                Text(
                    text = "Upload Your ID",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Please upload a clear photo of your government-issued ID",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextLight,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ID Upload Card
                UploadCard(
                    isUploaded = idImageUri != null,
                    title = "Government ID",
                    subtitle = "PNG, JPG up to 5MB",
                    uploadedText = "ID document uploaded ✓",
                    icon = Icons.Outlined.Upload,
                    onClick = {
                        showImagePicker = "id"
                        imagePickerLauncher.launch("image/*")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Selfie Upload Card
                UploadCard(
                    isUploaded = selfieUri != null,
                    title = "Take a Selfie",
                    subtitle = "Make sure your face is clearly visible",
                    uploadedText = "Selfie photo uploaded ✓",
                    icon = Icons.Outlined.Camera,
                    onClick = {
                        showImagePicker = "selfie"
                        imagePickerLauncher.launch("image/*")
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Security notice
                SecurityNotice()

                Spacer(modifier = Modifier.height(24.dp))

                // Continue button
                PrimaryButton(
                    text = "Continue",
                    enabled = idImageUri != null && selfieUri != null && !isLoading,
                    isLoading = isLoading,
                    onClick = {
                        if (idImageUri != null && selfieUri != null) {
                            onIdUploaded(idImageUri!!, selfieUri!!)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please upload both ID and selfie photos")
                            }
                        }
                    }
                )
            }
        }
    }
}

// STEP 2: Add Phone Number
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycAddPhoneScreen(
    onPhoneAdded: (String) -> Unit,
    onNavigateBack: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var phoneNumber by remember { mutableStateOf("") }
    var isValidPhone by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Log when screen is displayed
    LaunchedEffect(Unit) {
        Log.d("KYC_UI", "KycAddPhoneScreen is now displayed")
    }

    // Validate phone number
    LaunchedEffect(phoneNumber) {
        isValidPhone = phoneNumber.length >= 10
    }

    // Floating animation
    val infiniteTransition = rememberInfiniteTransition()
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(BackgroundGradientStart, BackgroundGradientEnd)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Back button and progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BackButton(onClick = onNavigateBack)

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Step 2 of 4",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextLight,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "50%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPink
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { 0.50f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PrimaryPink,
                    trackColor = Color(0xFFFFE0EB),
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Floating icon with glow
                FloatingIcon(
                    icon = Icons.Default.Phone,
                    offset = floatingOffset
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Add Phone Number",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter your phone number for account verification",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextLight,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Phone input card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = {
                                if (it.length <= 13) phoneNumber = it
                            },
                            label = { Text("Phone Number", color = TextLight) },
                            placeholder = { Text("+254 712 345 678") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Phone",
                                    tint = PrimaryPink
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPink,
                                unfocusedBorderColor = Color(0xFFFFE0EB),
                                focusedLabelColor = PrimaryPink,
                                cursorColor = PrimaryPink
                            ),
                            isError = phoneNumber.isNotEmpty() && !isValidPhone,
                            supportingText = {
                                if (phoneNumber.isNotEmpty() && !isValidPhone) {
                                    Text(
                                        "Please enter a valid phone number",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Continue button
                PrimaryButton(
                    text = "Send Verification Code",
                    enabled = isValidPhone && !isLoading,
                    isLoading = isLoading,
                    onClick = {
                        if (isValidPhone) {
                            onPhoneAdded(phoneNumber)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please enter a valid phone number")
                            }
                        }
                    }
                )
            }
        }
    }
}

// STEP 3: Verify Phone with OTP
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycVerifyPhoneScreen(
    phoneNumber: String,
    onOtpVerified: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onResendCode: () -> Unit = {},
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var otp by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(BackgroundGradientStart, BackgroundGradientEnd)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Back button and progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BackButton(onClick = onNavigateBack)

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Step 3 of 4",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextLight,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "75%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPink
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { 0.75f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = PrimaryPink,
                        trackColor = Color(0xFFFFE0EB),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Pulsing shield icon
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .scale(scale)
                                .alpha(0.2f)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(AccentPink, Color.Transparent)
                                    ),
                                    shape = CircleShape
                                )
                        )

                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(LightPink, Color(0xFFFFE0EB))
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Verify",
                                modifier = Modifier.size(32.dp),
                                tint = PrimaryPink
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Enter Verification Code",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Enter the 6-digit code sent to\n$phoneNumber",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextLight,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // OTP Input boxes
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        repeat(6) { index ->
                            OtpDigitBox(
                                digit = if (index < otp.length) otp[index].toString() else "",
                                isFocused = index == otp.length,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Number pad
                    NumberPadForOtp(
                        onNumberClick = { digit ->
                            if (otp.length < 6) {
                                otp += digit
                                if (otp.length == 6) {
                                    // Auto-verify when complete
                                    onOtpVerified(otp)
                                }
                            }
                        },
                        onBackspaceClick = {
                            if (otp.isNotEmpty()) {
                                otp = otp.dropLast(1)
                            }
                        }
                    )
                }

                // Button fixed at bottom
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Verify button
                    PrimaryButton(
                        text = "Verify Code",
                        icon = Icons.Default.CheckCircle,
                        enabled = otp.length == 6 && !isLoading,
                        isLoading = isLoading,
                        onClick = {
                            if (otp.length == 6) {
                                onOtpVerified(otp)
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please enter the complete 6-digit code")
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = {
                            onResendCode()
                            scope.launch {
                                snackbarHostState.showSnackbar("Verification code resent!")
                            }
                        }
                    ) {
                        Text(
                            "Resend Code",
                            color = PrimaryPink,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// STEP 4: Create PIN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycCreatePinScreen(
    onPinCreated: (String) -> Unit,
    onNavigateBack: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isCreatingPin by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Floating animation
    val infiniteTransition = rememberInfiniteTransition()
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(BackgroundGradientStart, BackgroundGradientEnd)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Back button and progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BackButton(onClick = onNavigateBack)

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Step 4 of 4",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextLight,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "100%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPink
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PrimaryPink,
                    trackColor = Color(0xFFFFE0EB),
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Floating icon
                FloatingIcon(
                    icon = Icons.Default.Lock,
                    offset = floatingOffset
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isCreatingPin) "Create Your PIN" else "Confirm Your PIN",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isCreatingPin)
                        "Create a 4-digit PIN to secure your account"
                    else
                        "Please re-enter your PIN to confirm",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextLight,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // PIN Display
                val currentPin = if (isCreatingPin) pin else confirmPin
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 40.dp)
                ) {
                    repeat(4) { index ->
                        PinDot(
                            isFilled = index < currentPin.length,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Number pad
                NumberPadForOtp(
                    onNumberClick = { digit ->
                        if (isCreatingPin) {
                            if (pin.length < 4) {
                                pin += digit
                                if (pin.length == 4) {
                                    // Move to confirm
                                    scope.launch {
                                        delay(200)
                                        isCreatingPin = false
                                    }
                                }
                            }
                        } else {
                            if (confirmPin.length < 4) {
                                confirmPin += digit
                                if (confirmPin.length == 4) {
                                    // Verify match
                                    scope.launch {
                                        delay(200)
                                        if (pin == confirmPin) {
                                            onPinCreated(pin)
                                        } else {
                                            snackbarHostState.showSnackbar("PINs don't match. Try again.")
                                            confirmPin = ""
                                            isCreatingPin = true
                                            pin = ""
                                        }
                                    }
                                }
                            }
                        }
                    },
                    onBackspaceClick = {
                        if (isCreatingPin) {
                            if (pin.isNotEmpty()) {
                                pin = pin.dropLast(1)
                            }
                        } else {
                            if (confirmPin.isNotEmpty()) {
                                confirmPin = confirmPin.dropLast(1)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Complete button (shown only when confirming and complete)
                if (!isCreatingPin && confirmPin.length == 4) {
                    PrimaryButton(
                        text = "Complete Registration",
                        icon = Icons.Default.Check,
                        enabled = !isLoading,
                        isLoading = isLoading,
                        onClick = {
                            if (pin == confirmPin) {
                                onPinCreated(pin)
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("PINs don't match. Try again.")
                                    confirmPin = ""
                                    isCreatingPin = true
                                    pin = ""
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

// ==================== REUSABLE COMPONENTS ====================

@Composable
private fun ProgressHeader(
    stepText: String,
    progressText: String,
    progress: Float
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stepText,
                style = MaterialTheme.typography.bodyMedium,
                color = TextLight,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = progressText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryPink
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = PrimaryPink,
            trackColor = Color(0xFFFFE0EB),
        )
    }
}

@Composable
private fun FloatingIcon(
    icon: ImageVector,
    offset: Float
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.offset(y = offset.dp)
    ) {
        // Glow effect
        Box(
            modifier = Modifier
                .size(100.dp)
                .alpha(0.3f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentPink.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(LightPink, Color(0xFFFFE0EB))
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = PrimaryPink
            )
        }
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .background(Color.White, CircleShape)
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = PrimaryPink
        )
    }
}

@Composable
private fun SecurityNotice() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightPink.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, AccentPink.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Security",
                tint = PrimaryPink,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Your data is encrypted and secure",
                style = MaterialTheme.typography.bodyMedium,
                color = DarkPink,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryPink,
            disabledContainerColor = Color(0xFFFFE0EB)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 10.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (icon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UploadCard(
    isUploaded: Boolean,
    title: String,
    subtitle: String,
    uploadedText: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isUploaded) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUploaded) LightPink else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isUploaded) 2.dp else 1.dp,
            color = if (isUploaded) SuccessGreen else Color(0xFFFFE0EB)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUploaded) 8.dp else 2.dp
        )
    ) {
        Box(modifier = Modifier.padding(24.dp)) {
            if (isUploaded) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(SuccessGreen.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Uploaded",
                            tint = SuccessGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = uploadedText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(LightPink, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            modifier = Modifier.size(28.dp),
                            tint = PrimaryPink
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLight,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun OtpDigitBox(
    digit: String,
    isFocused: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (digit.isNotEmpty()) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .border(
                width = if (isFocused) 2.dp else 1.5.dp,
                color = if (digit.isNotEmpty()) PrimaryPink
                else if (isFocused) AccentPink
                else Color(0xFFFFE0EB),
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = if (digit.isNotEmpty()) LightPink.copy(alpha = 0.3f) else Color.White,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (digit.isNotEmpty()) PrimaryPink else TextLight
        )
    }
}

@Composable
private fun PinDot(
    isFilled: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isFilled) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isFilled) PrimaryPink else Color(0xFFFFE0EB)
            )
    )
}

@Composable
private fun NumberPadForOtp(
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonSize = 64.dp
    val buttonSpacing = 16.dp

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(buttonSpacing)
    ) {
        // Rows 1-3 (numbers 1-9)
        for (row in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing, Alignment.CenterHorizontally)
            ) {
                for (col in 1..3) {
                    val number = (row * 3 + col).toString()
                    OtpNumberButton(
                        text = number,
                        onClick = { onNumberClick(number) },
                        modifier = Modifier.size(buttonSize)
                    )
                }
            }
        }

        // Last row (0 and backspace)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing, Alignment.CenterHorizontally)
        ) {
            Spacer(modifier = Modifier.size(buttonSize))
            OtpNumberButton(
                text = "0",
                onClick = { onNumberClick("0") },
                modifier = Modifier.size(buttonSize)
            )
            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .clip(CircleShape)
                    .clickable(onClick = onBackspaceClick)
                    .background(LightPink),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Backspace",
                    modifier = Modifier.size(24.dp),
                    tint = PrimaryPink
                )
            }
        }
    }
}

@Composable
private fun OtpNumberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Surface(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier.scale(scale),
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color(0xFFFFE0EB), CircleShape)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryPink
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}