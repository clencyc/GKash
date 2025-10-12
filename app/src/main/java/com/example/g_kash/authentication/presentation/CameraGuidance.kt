package com.example.g_kash.authentication.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Enhanced camera guidance with real-time overlays and feedback
 * Includes face detection indicators, photo quality guidance, and success celebrations
 */

/**
 * Camera viewfinder overlay with guidance
 */
@Composable
fun CameraViewfinderOverlay(
    isIdScan: Boolean = true,
    faceDetected: Boolean = false,
    photoQuality: PhotoQuality = PhotoQuality.UNKNOWN,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )
        
        if (isIdScan) {
            // ID Card viewfinder
            IdCardViewfinder(
                photoQuality = photoQuality,
                screenWidth = screenWidth,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // Face detection viewfinder
            FaceDetectionOverlay(
                faceDetected = faceDetected,
                photoQuality = photoQuality,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // Guidance text at top
        GuidanceText(
            isIdScan = isIdScan,
            faceDetected = faceDetected,
            photoQuality = photoQuality,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        // Quality indicators at bottom
        QualityIndicators(
            photoQuality = photoQuality,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * ID Card viewfinder with corner guides
 */
@Composable
private fun IdCardViewfinder(
    photoQuality: PhotoQuality,
    screenWidth: Dp,
    modifier: Modifier = Modifier
) {
    val cardWidth = (screenWidth * 0.8f)
    val cardHeight = cardWidth * 0.63f // Standard ID card ratio
    
    val borderColor = when (photoQuality) {
        PhotoQuality.GOOD -> Color(0xFF10B981)
        PhotoQuality.POOR -> Color(0xFFEF4444)
        PhotoQuality.FAIR -> Color(0xFFF59E0B)
        PhotoQuality.UNKNOWN -> Color.White
    }
    
    val animatedStroke by animateFloatAsState(
        targetValue = when (photoQuality) {
            PhotoQuality.GOOD -> 4f
            PhotoQuality.POOR -> 3f
            PhotoQuality.FAIR -> 3f
            PhotoQuality.UNKNOWN -> 2f
        },
        animationSpec = tween(300)
    )
    
    Box(
        modifier = modifier
            .size(cardWidth, cardHeight)
            .border(
                width = animatedStroke.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .semantics {
                contentDescription = "Position your ID card within the frame"
            }
    ) {
        // Corner guides
        CornerGuides(
            borderColor = borderColor,
            modifier = Modifier.fillMaxSize()
        )
        
        // Center crosshair for alignment
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val crosshairSize = 20.dp.toPx()
            
            drawLine(
                color = borderColor,
                start = Offset(centerX - crosshairSize, centerY),
                end = Offset(centerX + crosshairSize, centerY),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = borderColor,
                start = Offset(centerX, centerY - crosshairSize),
                end = Offset(centerX, centerY + crosshairSize),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

/**
 * Face detection overlay with animated circle
 */
@Composable
private fun FaceDetectionOverlay(
    faceDetected: Boolean,
    photoQuality: PhotoQuality,
    modifier: Modifier = Modifier
) {
    val circleSize = 200.dp
    
    val borderColor = if (faceDetected) {
        when (photoQuality) {
            PhotoQuality.GOOD -> Color(0xFF10B981)
            PhotoQuality.FAIR -> Color(0xFFF59E0B)
            PhotoQuality.POOR -> Color(0xFFEF4444)
            PhotoQuality.UNKNOWN -> Color(0xFF3B82F6)
        }
    } else {
        Color.White
    }
    
    // Pulsing animation when face detected
    val scale by animateFloatAsState(
        targetValue = if (faceDetected) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = modifier
            .size(circleSize)
            .scale(scale)
            .semantics {
                contentDescription = if (faceDetected) "Face detected" else "Position your face in the circle"
            },
        contentAlignment = Alignment.Center
    ) {
        // Outer circle
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawCircle(
                color = borderColor,
                radius = size.width / 2 - 8.dp.toPx(),
                style = Stroke(width = 4.dp.toPx())
            )
        }
        
        // Face detection indicators
        if (faceDetected) {
            FaceDetectionIndicators(
                color = borderColor,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Corner guides for ID card positioning
 */
@Composable
private fun CornerGuides(
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cornerSize = 20.dp.toPx()
        val strokeWidth = 3.dp.toPx()
        
        // Top-left corner
        drawLine(
            color = borderColor,
            start = Offset(0f, cornerSize),
            end = Offset(0f, 0f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = borderColor,
            start = Offset(0f, 0f),
            end = Offset(cornerSize, 0f),
            strokeWidth = strokeWidth
        )
        
        // Top-right corner
        drawLine(
            color = borderColor,
            start = Offset(size.width - cornerSize, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = borderColor,
            start = Offset(size.width, 0f),
            end = Offset(size.width, cornerSize),
            strokeWidth = strokeWidth
        )
        
        // Bottom-left corner
        drawLine(
            color = borderColor,
            start = Offset(0f, size.height - cornerSize),
            end = Offset(0f, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = borderColor,
            start = Offset(0f, size.height),
            end = Offset(cornerSize, size.height),
            strokeWidth = strokeWidth
        )
        
        // Bottom-right corner
        drawLine(
            color = borderColor,
            start = Offset(size.width - cornerSize, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = borderColor,
            start = Offset(size.width, size.height - cornerSize),
            end = Offset(size.width, size.height),
            strokeWidth = strokeWidth
        )
    }
}

/**
 * Face detection visual indicators
 */
@Composable
private fun FaceDetectionIndicators(
    color: Color,
    modifier: Modifier = Modifier
) {
    val rotation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        )
    )
    
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2 - 20.dp.toPx()
        
        // Rotating detection points
        for (i in 0..7) {
            val angle = (rotation + i * 45) * (Math.PI / 180)
            val x = center.x + cos(angle).toFloat() * radius
            val y = center.y + sin(angle).toFloat() * radius
            
            drawCircle(
                color = color,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

/**
 * Dynamic guidance text based on scan state
 */
@Composable
private fun GuidanceText(
    isIdScan: Boolean,
    faceDetected: Boolean,
    photoQuality: PhotoQuality,
    modifier: Modifier = Modifier
) {
    val text = if (isIdScan) {
        when (photoQuality) {
            PhotoQuality.GOOD -> "Perfect! Hold steady and tap to capture"
            PhotoQuality.FAIR -> "Good - adjust lighting for better quality"
            PhotoQuality.POOR -> "Move to better lighting area"
            PhotoQuality.UNKNOWN -> "Position your ID within the frame"
        }
    } else {
        when {
            !faceDetected -> "Position your face in the circle"
            photoQuality == PhotoQuality.GOOD -> "Great! Face detected clearly ✨"
            photoQuality == PhotoQuality.FAIR -> "Good - try improving lighting"
            photoQuality == PhotoQuality.POOR -> "Face detected - need better lighting"
            else -> "Look straight at the camera"
        }
    }
    
@OptIn(ExperimentalAnimationApi::class)
    AnimatedContent(
        targetState = text,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) with 
            fadeOut(animationSpec = tween(300))
        },
        modifier = modifier.padding(16.dp)
    ) { currentText ->
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.7f)
            ),
            modifier = Modifier.semantics {
                contentDescription = "Camera guidance: $currentText"
            }
        ) {
            Text(
                text = currentText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

/**
 * Photo quality indicators
 */
@Composable
private fun QualityIndicators(
    photoQuality: PhotoQuality,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(16.dp)
            .semantics {
                contentDescription = "Photo quality: ${photoQuality.name.lowercase()}"
            },
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QualityIndicator(
            icon = Icons.Default.WbSunny,
            label = "Lighting",
            isGood = photoQuality != PhotoQuality.POOR
        )
        QualityIndicator(
            icon = Icons.Default.CenterFocusStrong,
            label = "Focus",
            isGood = photoQuality == PhotoQuality.GOOD
        )
        QualityIndicator(
            icon = Icons.Default.Visibility,
            label = "Clarity",
            isGood = photoQuality != PhotoQuality.POOR
        )
    }
}

@Composable
private fun QualityIndicator(
    icon: ImageVector,
    label: String,
    isGood: Boolean
) {
    val color = if (isGood) Color(0xFF10B981) else Color(0xFF6B7280)
    
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
    }
}

/**
 * Success celebration animation
 */
@Composable
fun SuccessCelebration(
    visible: Boolean,
    message: String = "Success!",
    onComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showFireworks by remember { mutableStateOf(false) }
    var showCheckmark by remember { mutableStateOf(false) }
    
    LaunchedEffect(visible) {
        if (visible) {
            showCheckmark = true
            delay(500)
            showFireworks = true
            delay(2000)
            onComplete()
        } else {
            showFireworks = false
            showCheckmark = false
        }
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            // Fireworks effect
            if (showFireworks) {
                FireworksEffect()
            }
            
            // Success checkmark and message
            AnimatedVisibility(
                visible = showCheckmark,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Large checkmark
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF10B981),
                                        Color(0xFF059669)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Success message
                    Text(
                        text = message,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Fireworks particles effect
 */
@Composable
private fun FireworksEffect() {
    val particles = remember { (1..30).map { FireworkParticle() } }
    
    LaunchedEffect(Unit) {
        particles.forEach { it.reset() }
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            particle.update()
            drawCircle(
                color = particle.color,
                radius = particle.size.toPx(),
                center = Offset(
                    particle.x * size.width,
                    particle.y * size.height
                )
            )
        }
    }
}

private class FireworkParticle {
    var x = 0.5f
    var y = 0.5f
    var velocityX = 0f
    var velocityY = 0f
    var size = 3.dp
    var color = Color.White
    var life = 1f
    
    fun reset() {
        x = 0.5f + (Math.random().toFloat() - 0.5f) * 0.2f
        y = 0.5f + (Math.random().toFloat() - 0.5f) * 0.2f
        velocityX = (Math.random().toFloat() - 0.5f) * 0.01f
        velocityY = (Math.random().toFloat() - 0.5f) * 0.01f
        color = listOf(
            Color(0xFFFFD700), Color(0xFFFF6B6B), Color(0xFF4ECDC4),
            Color(0xFF45B7D1), Color(0xFF96CEB4), Color(0xFFFFF3E0)
        ).random()
        life = 1f
    }
    
    fun update() {
        x += velocityX
        y += velocityY
        velocityY += 0.0001f // gravity
        life -= 0.01f
        
        if (life <= 0) {
            reset()
        }
        
        color = color.copy(alpha = life.coerceIn(0f, 1f))
    }
}

enum class PhotoQuality {
    UNKNOWN, POOR, FAIR, GOOD
}