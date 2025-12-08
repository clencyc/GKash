package com.example.g_kash.authentication.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush

// Pink color scheme to match other screens
private val PrimaryPink = Color(0xFFD91A5B)
private val LightPink = Color(0xFFFFF0F5)
private val DarkPink = Color(0xFFC4164E)
private val AccentPink = Color(0xFFFF6B9D)
private val TextDark = Color(0xFF1A1A1A)
private val TextLight = Color(0xFF6B6B6B)

/**
 * Onboarding screen shown to unauthenticated users
 * Displays welcome message and navigation options to login or register
 */
@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        LightPink.copy(alpha = 0.3f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .verticalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App branding
            Text(
                text = "GKash",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryPink,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Your Personal Financial Companion",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextLight,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Features list
            FeatureItem(
                title = "Secure Banking",
                description = "Keep your money safe with encryption"
            )

            FeatureItem(
                title = "Easy Transfers",
                description = "Send and receive money instantly"
            )

            FeatureItem(
                title = "Investment Tools",
                description = "Grow your wealth with smart investments"
            )

            FeatureItem(
                title = "Financial Learning",
                description = "Learn money management from experts"
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Register Button (Primary)
            Button(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPink
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Text(
                    text = "Create New Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Button (Secondary)
            OutlinedButton(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryPink
                )
            ) {
                Text(
                    text = "Sign In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "By continuing, you agree to our Terms of Service and Privacy Policy",
                fontSize = 12.sp,
                color = TextLight,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FeatureItem(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "✓ $title",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark
        )
        Text(
            text = description,
            fontSize = 14.sp,
            color = TextLight,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
