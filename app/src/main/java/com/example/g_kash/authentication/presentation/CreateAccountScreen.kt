package com.example.g_kash.authentication.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    onNavigateToPin: (String) -> Unit,
    viewModel: CreateAccountViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.NavigateToPin -> {
                    viewModel.getCurrentUserId()?.let { userId ->
                        onNavigateToPin(userId)
                    }
                }
                else -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFF1E3A8A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "G$",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Create Account",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = "Sign up to learn, invest, grow",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Name Field
        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::updateName,
            label = { Text("Name") },
            placeholder = { Text("John Doe") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1E3A8A),
                focusedLabelColor = Color(0xFF1E3A8A)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Phone Number Field
        OutlinedTextField(
            value = uiState.phoneNumber,
            onValueChange = viewModel::updatePhoneNumber,
            label = { Text("Phone Number") },
            placeholder = { Text("+254 *****") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1E3A8A),
                focusedLabelColor = Color(0xFF1E3A8A)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ID Number Field
        OutlinedTextField(
            value = uiState.idNumber,
            onValueChange = viewModel::updateIdNumber,
            label = { Text("ID Number") },
            placeholder = { Text("12345678") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1E3A8A),
                focusedLabelColor = Color(0xFF1E3A8A)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Terms and Conditions
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = uiState.isTermsAccepted,
                onCheckedChange = { viewModel.toggleTermsAcceptance() },
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1E3A8A))
            )
            Text(
                text = "Accept Terms and Conditions",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Error Message
        uiState.error?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Set Pin Button
        Button(
            onClick = viewModel::createAccount,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1E3A8A)
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = !uiState.isLoading && uiState.isTermsAccepted
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "Set Pin",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CreatePinScreen(
    onNavigateToConfirmPin: (String, String) -> Unit,
    userId: String,
    viewModel: CreatePinViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState

    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.NavigateToConfirmPin -> {
                    onNavigateToConfirmPin(userId, viewModel.getPin())
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(uiState.isPinComplete) {
        if (uiState.isPinComplete) {
            kotlinx.coroutines.delay(300)
            viewModel.proceedToConfirmPin()
        }
    }

    PinInputScreen(
        title = "Create Pin",
        subtitle = "Learn, Invest,Grow",
        pin = uiState.pin,
        onPinChange = viewModel::updatePin,
        buttonText = "Next"
    )
}

@Composable
fun ConfirmPinScreen(
    onNavigateToApp: () -> Unit,
    userId: String,
    originalPin: String,
    viewModel: ConfirmPinViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState

    LaunchedEffect(userId, originalPin) {
        viewModel.setUserIdAndPin(userId, originalPin)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.NavigateToApp -> {
                    onNavigateToApp()
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(uiState.isPinComplete) {
        if (uiState.isPinComplete) {
            kotlinx.coroutines.delay(300)
            viewModel.confirmPin()
        }
    }

    PinInputScreen(
        title = "Confirm Pin",
        subtitle = "Learn, Invest,Grow",
        pin = uiState.confirmPin,
        onPinChange = viewModel::updateConfirmPin,
        buttonText = "Continue to App",
        isLoading = uiState.isLoading,
        error = uiState.error
    )
}

@Composable
private fun PinInputScreen(
    title: String,
    subtitle: String,
    pin: String,
    onPinChange: (String) -> Unit,
    buttonText: String,
    isLoading: Boolean = false,
    error: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFF1E3A8A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "G$",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(60.dp))

        // Pin Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            if (index < pin.length) Color(0xFF1E3A8A) else Color.Gray.copy(0.3f),
                            CircleShape
                        )
                )
            }
        }

        // Error Message
        error?.let { errorMsg ->
            Text(
                text = errorMsg,
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Keypad
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            items((1..9).toList()) { number ->
                KeypadButton(
                    text = number.toString(),
                    onClick = {
                        if (pin.length < 4) {
                            onPinChange(pin + number)
                        }
                    }
                )
            }

            // Empty space
            item { Spacer(modifier = Modifier) }

            // Zero
            item {
                KeypadButton(
                    text = "0",
                    onClick = {
                        if (pin.length < 4) {
                            onPinChange(pin + "0")
                        }
                    }
                )
            }

            // Backspace
            item {
                KeypadButton(
                    icon = Icons.Default.Backspace,
                    onClick = {
                        if (pin.isNotEmpty()) {
                            onPinChange(pin.dropLast(1))
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Next Button
        Button(
            onClick = { /* Handled by LaunchedEffect in parent composables */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1E3A8A)
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = pin.length == 4 && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = buttonText,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun KeypadButton(
    text: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when {
            text != null -> {
                Text(
                    text = text,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
            icon != null -> {
                Icon(
                    imageVector = icon,
                    contentDescription = "Backspace",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}