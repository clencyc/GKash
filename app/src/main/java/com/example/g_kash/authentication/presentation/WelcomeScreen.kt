package com.example.g_kash.authentication.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Warp-style Welcome Screen - The entry point to G-Kash onboarding experience
 * Provides engaging introduction with security assurance and clear call-to-action
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showContent by remember { mutableStateOf(false) }
    var showSecurityBadge by remember { mutableStateOf(false) }
    var showPrivacyNotice by remember { mutableStateOf(false) }
    
    // Entrance animations
    LaunchedEffect(Unit) {
        delay(300)
        showContent = true
        delay(600)
        showSecurityBadge = true
        delay(900)
        showPrivacyNotice = true
    }
    
    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        modifier = modifier.semantics { 
            contentDescription = "Welcome to G-Kash. Start your secure account setup"
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF8FAFC),
                                Color(0xFFE2E8F0)
                            )
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                
                // Top Section - Branding & Hero
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(800)) + 
                           slideInVertically(animationSpec = tween(800)) { -it }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 40.dp)
                    ) {
                        // App Logo with subtle animation
                        val scale by animateFloatAsState(
                            targetValue = if (showContent) 1f else 0.8f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .scale(scale)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF6366F1),
                                            Color(0xFF4338CA)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Welcome Text
                        Text(
                            text = "Welcome to G-Kash!",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Let's get you set up securely in just a few minutes.",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Feature highlights
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            FeatureHighlight(
                                icon = Icons.Default.Security,
                                title = "Secure",
                                description = "Bank-level security"
                            )
                            FeatureHighlight(
                                icon = Icons.Default.Speed,
                                title = "Fast",
                                description = "Setup in minutes"
                            )
                            FeatureHighlight(
                                icon = Icons.Default.Verified,
                                title = "Trusted",
                                description = "KYC compliant"
                            )
                        }
                    }
                }
                
                // Middle Section - Process Preview
                AnimatedVisibility(
                    visible = showSecurityBadge,
                    enter = fadeIn(animationSpec = tween(600)) + 
                           slideInVertically(animationSpec = tween(600)) { it / 2 }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Quick Setup Process",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B),
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Process steps preview
                            ProcessStep(
                                step = "1",
                                title = "Verify Identity",
                                description = "Quick photo of your ID",
                                icon = Icons.Default.CameraAlt
                            )
                            
                            ProcessStep(
                                step = "2", 
                                title = "Phone Verification",
                                description = "Secure your account",
                                icon = Icons.Default.Phone
                            )
                            
                            ProcessStep(
                                step = "3",
                                title = "Create PIN",
                                description = "Easy & secure access",
                                icon = Icons.Default.Lock,
                                isLast = true
                            )
                        }
                    }
                }
                
                // Bottom Section - CTA & Privacy
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Privacy Notice
                    AnimatedVisibility(
                        visible = showPrivacyNotice,
                        enter = fadeIn(animationSpec = tween(600))
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFEEF2FF)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Security",
                                    tint = Color(0xFF6366F1),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Your data is encrypted and used only for verification",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF475569),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    
                    // Get Started Button
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(animationSpec = tween(800, delayMillis = 400)) +
                               slideInVertically(animationSpec = tween(800, delayMillis = 400)) { it }
                    ) {
                        Button(
                            onClick = onGetStarted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .semantics {
                                    contentDescription = "Get Started with account setup"
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1),
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Get Started",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Support link
                    TextButton(
                        onClick = { /* Handle support */ },
                        modifier = Modifier.semantics {
                            contentDescription = "Need help? Contact support"
                        }
                    ) {
                        Text(
                            text = "Need help? Contact Support",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureHighlight(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color(0xFFEEF2FF),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProcessStep(
    step: String,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Step indicator
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    Color(0xFFEEF2FF),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E293B)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B)
            )
        }
        
        // Step number
        Text(
            text = step,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6366F1)
        )
    }
    
    if (!isLast) {
        Spacer(modifier = Modifier.height(16.dp))
    }
}