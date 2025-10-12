package com.example.g_kash.authentication.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Enhanced feedback system with encouraging micro-copy and helpful error messages
 * Following Warp-style UX patterns for user engagement and retention
 */

// Encouraging messages for different stages
object MicroCopy {
    // Welcome and onboarding
    val welcomeMessages = listOf(
        "Let's get you set up securely in just a few minutes!",
        "Your digital wallet journey starts here 🚀",
        "Bank-level security, simple setup process"
    )
    
    // ID Upload encouragement
    val idUploadMessages = listOf(
        "Take a clear, well-lit photo for best results",
        "Hold steady and make sure all details are visible",
        "Good lighting makes verification faster!"
    )
    
    // Selfie encouragement  
    val selfieMessages = listOf(
        "Look straight at the camera and smile!",
        "Great! Face detected clearly ✨",
        "Perfect positioning - you're doing great!"
    )
    
    // Phone verification
    val phoneMessages = listOf(
        "We'll send a secure code to verify it's really you",
        "Phone verified! You're almost there 🎉",
        "This helps keep your account secure"
    )
    
    // OTP verification
    val otpMessages = listOf(
        "Check your messages for the 6-digit code",
        "Code sent! It should arrive within 30 seconds",
        "Having trouble? We can resend the code"
    )
    
    // PIN creation
    val pinMessages = listOf(
        "Choose something memorable but secure",
        "This PIN will protect your account",
        "Almost done - you're doing great!"
    )
    
    // Success messages
    val successMessages = listOf(
        "Fantastic! Everything looks perfect",
        "Verification complete - welcome aboard! 🎊",
        "Your account is ready to go!"
    )
}

// Enhanced error messages with helpful guidance
object ErrorMessages {
    fun getIdUploadError(errorType: String): String {
        return when (errorType) {
            "blurry" -> "Photo looks blurry - try steadying your phone or improving lighting"
            "dark" -> "Photo is too dark - find better lighting and try again"
            "glare" -> "Too much glare - adjust the angle to avoid reflections"
            "incomplete" -> "Make sure all corners of your ID are visible in the photo"
            "format" -> "Please use a supported image format (JPG, PNG)"
            "size" -> "Image file is too large - try taking a new photo"
            "network" -> "Connection issue - check your internet and try again"
            "duplicate" -> "This ID is already registered - try logging in instead?"
            else -> "Upload failed - try better lighting or retake the photo"
        }
    }
    
    fun getPhoneError(errorType: String): String {
        return when (errorType) {
            "invalid" -> "Please enter a valid phone number with country code"
            "exists" -> "This number is already registered - try logging in?"
            "blocked" -> "This number is temporarily blocked - contact support"
            "network" -> "Connection issue - please try again"
            else -> "Phone verification failed - please check the number and retry"
        }
    }
    
    fun getOtpError(errorType: String): String {
        return when (errorType) {
            "invalid" -> "That code doesn't match - please double-check and try again"
            "expired" -> "Code expired - we'll send you a fresh one"
            "attempts" -> "Too many attempts - we'll send a new code in 60 seconds"
            "network" -> "Connection issue - verification will retry automatically"
            else -> "Verification failed - try entering the code again"
        }
    }
    
    fun getPinError(errorType: String): String {
        return when (errorType) {
            "weak" -> "Try avoiding simple patterns like 1234 or 0000"
            "mismatch" -> "PINs don't match - please try entering them again"
            "network" -> "Connection issue - your PIN will be saved automatically"
            else -> "PIN setup failed - please try again"
        }
    }
}

/**
 * Encouraging success message component with animation
 */
@Composable
fun SuccessMessage(
    message: String,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + 
               slideInVertically(animationSpec = tween(500)) { -it / 2 },
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics {
                    contentDescription = "Success: $message"
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFECFDF5)
            ),
            border = BorderStroke(1.dp, Color(0xFF10B981))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated success icon
                val scale by animateFloatAsState(
                    targetValue = if (visible) 1f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .scale(scale)
                        .background(Color(0xFF10B981), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF065F46),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Enhanced error message component with helpful guidance
 */
@Composable
fun ErrorMessage(
    message: String,
    visible: Boolean,
    onRetry: (() -> Unit)? = null,
    onSupport: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + 
               slideInVertically(animationSpec = tween(500)) { it / 2 },
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics {
                    contentDescription = "Error: $message"
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFEF2F2)
            ),
            border = BorderStroke(1.dp, Color(0xFFF87171))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF991B1B),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Action buttons
                if (onRetry != null || onSupport != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        onRetry?.let {
                            TextButton(
                                onClick = it,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color(0xFFDC2626)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Try Again")
                            }
                        }
                        
                        onSupport?.let {
                            TextButton(
                                onClick = it,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color(0xFF6B7280)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Help,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Get Help")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Encouraging tip component for guidance
 */
@Composable
fun EncouragingTip(
    tip: String,
    icon: ImageVector = Icons.Default.Lightbulb,
    visible: Boolean = true,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .semantics {
                    contentDescription = "Tip: $tip"
                },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFEFBE8)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Tip",
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF92400E),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Progress achievement notification
 */
@Composable
fun ProgressAchievement(
    message: String,
    progress: Float,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(600)) + 
               slideInVertically(animationSpec = tween(600)) { -it },
        exit = fadeOut(animationSpec = tween(400))
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics {
                    contentDescription = "Progress: $message"
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF0F9FF)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF3B82F6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Progress",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E3A8A)
                        )
                        
                        Text(
                            text = "${(progress * 100).toInt()}% complete",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF3730A3)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress bar
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(durationMillis = 1000)
                )
                
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = Color(0xFF3B82F6),
                    trackColor = Color(0xFFE5E7EB),
                )
            }
        }
    }
}

/**
 * Security assurance badge
 */
@Composable
fun SecurityAssurance(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .semantics {
                contentDescription = "Your data is secure and encrypted"
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0FDF4)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.SecurityUpdate,
                contentDescription = "Secure",
                tint = Color(0xFF059669),
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "256-bit encryption • GDPR compliant",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF065F46),
                fontWeight = FontWeight.Medium
            )
        }
    }
}