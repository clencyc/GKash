package com.example.g_kash.authentication.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign

/**
 * Accessibility utilities for enhanced screen reader support,
 * high-contrast visuals, and inclusive design patterns
 */

/**
 * High contrast theme colors for accessibility
 */
object AccessibilityColors {
    val HighContrastPrimary = Color(0xFF000000)
    val HighContrastSecondary = Color(0xFFFFFFFF) 
    val HighContrastSuccess = Color(0xFF006600)
    val HighContrastError = Color(0xFFCC0000)
    val HighContrastWarning = Color(0xFFFF6600)
    val HighContrastBackground = Color(0xFFFFFFFF)
    val HighContrastSurface = Color(0xFFF0F0F0)
    val HighContrastBorder = Color(0xFF000000)
}

/**
 * Accessibility settings state
 */
@Composable
fun rememberAccessibilitySettings(): AccessibilitySettings {
    val context = LocalContext.current
    val accessibilityManager = LocalAccessibilityManager.current
    
    return remember {
        AccessibilitySettings(
            isScreenReaderEnabled = accessibilityManager != null,
            isHighContrastEnabled = false, // Can be determined from system settings
            isTtsEnabled = true // Can be configured
        )
    }
}

data class AccessibilitySettings(
    val isScreenReaderEnabled: Boolean = false,
    val isHighContrastEnabled: Boolean = false,
    val isTtsEnabled: Boolean = false,
    val largeTextEnabled: Boolean = false,
    val reduceMotionEnabled: Boolean = false
)

/**
 * Text-to-speech manager for voice announcements
 */
class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    
    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
            }
        }
    }
    
    fun speak(text: String, priority: Int = TextToSpeech.QUEUE_FLUSH) {
        if (isInitialized) {
            tts?.speak(text, priority, null, null)
        }
    }
    
    fun announce(text: String) {
        speak("Announcement: $text", TextToSpeech.QUEUE_ADD)
    }
    
    fun cleanup() {
        tts?.shutdown()
    }
}

/**
 * Haptic feedback manager
 */
class HapticManager(private val context: Context) {
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    
    fun success() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (vibrator as? Vibrator)?.vibrate(
                VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        }
    }
    
    fun error() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (vibrator as? Vibrator)?.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100), -1)
            )
        }
    }
    
    fun navigation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (vibrator as? Vibrator)?.vibrate(
                VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        }
    }
}

/**
 * Accessible button with enhanced semantic properties
 */
@Composable
fun AccessibleButton(
    onClick: () -> Unit,
    text: String,
    contentDescription: String = text,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    accessibilitySettings: AccessibilitySettings = AccessibilitySettings(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticManager = remember { HapticManager(context) }
    
    Button(
        onClick = {
            hapticManager.navigation()
            onClick()
        },
        enabled = enabled && !isLoading,
        modifier = modifier
            .semantics {
                this.contentDescription = if (isLoading) {
                    "Loading, $contentDescription"
                } else {
                    contentDescription
                }
                if (!enabled) {
                    this.disabled()
                }
                role = Role.Button
            }
            .focusable(),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (accessibilitySettings.isHighContrastEnabled) {
                AccessibilityColors.HighContrastPrimary
            } else {
                Color(0xFF6366F1)
            },
            contentColor = if (accessibilitySettings.isHighContrastEnabled) {
                AccessibilityColors.HighContrastSecondary
            } else {
                Color.White
            }
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (accessibilitySettings.isHighContrastEnabled) {
            androidx.compose.foundation.BorderStroke(2.dp, AccessibilityColors.HighContrastBorder)
        } else null
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = if (accessibilitySettings.isHighContrastEnabled) {
                    AccessibilityColors.HighContrastSecondary
                } else {
                    Color.White
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Accessible progress indicator with voice announcements
 */
@Composable
fun AccessibleProgressIndicator(
    progress: Float,
    stepText: String,
    progressText: String,
    accessibilitySettings: AccessibilitySettings = AccessibilitySettings(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }
    var lastAnnouncedProgress by remember { mutableStateOf(-1) }
    
    // Announce progress changes for screen readers
    LaunchedEffect(progress) {
        val currentProgressInt = (progress * 100).toInt()
        if (accessibilitySettings.isScreenReaderEnabled && 
            currentProgressInt != lastAnnouncedProgress &&
            currentProgressInt % 10 == 0) { // Announce every 10%
            ttsManager.announce("Progress: $currentProgressInt percent complete")
            lastAnnouncedProgress = currentProgressInt
        }
    }
    
    DisposableEffect(ttsManager) {
        onDispose {
            ttsManager.cleanup()
        }
    }
    
    Column(
        modifier = modifier
            .semantics {
                contentDescription = "$stepText, $progressText complete"
                stateDescription = "Progress indicator"
            }
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(if (accessibilitySettings.isHighContrastEnabled) 8.dp else 4.dp),
            color = if (accessibilitySettings.isHighContrastEnabled) {
                AccessibilityColors.HighContrastPrimary
            } else {
                MaterialTheme.colorScheme.primary
            },
            trackColor = if (accessibilitySettings.isHighContrastEnabled) {
                AccessibilityColors.HighContrastSurface
            } else {
                Color(0xFFE0E4E7)
            }
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stepText,
                style = MaterialTheme.typography.bodySmall,
                color = if (accessibilitySettings.isHighContrastEnabled) {
                    AccessibilityColors.HighContrastPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = progressText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (accessibilitySettings.isHighContrastEnabled) {
                    AccessibilityColors.HighContrastPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

/**
 * Accessible input field with enhanced labeling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    helperText: String = "",
    errorText: String = "",
    accessibilitySettings: AccessibilitySettings = AccessibilitySettings(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticManager = remember { HapticManager(context) }
    val isError = errorText.isNotEmpty()
    
    LaunchedEffect(isError) {
        if (isError) {
            hapticManager.error()
        }
    }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            isError = isError,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = label
                    if (helperText.isNotEmpty()) {
                        this.text = AnnotatedString("$label, $helperText")
                    }
                    if (isError) {
                        this.error(errorText)
                    }
                },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (accessibilitySettings.isHighContrastEnabled) {
                    AccessibilityColors.HighContrastPrimary
                } else {
                    MaterialTheme.colorScheme.primary
                },
                errorBorderColor = if (accessibilitySettings.isHighContrastEnabled) {
                    AccessibilityColors.HighContrastError
                } else {
                    MaterialTheme.colorScheme.error
                }
            ),
            shape = RoundedCornerShape(12.dp)
        )
        
        // Helper or error text
        if (helperText.isNotEmpty() || isError) {
            Text(
                text = if (isError) errorText else helperText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) {
                    if (accessibilitySettings.isHighContrastEnabled) {
                        AccessibilityColors.HighContrastError
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                } else {
                    if (accessibilitySettings.isHighContrastEnabled) {
                        AccessibilityColors.HighContrastPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                },
                modifier = Modifier
                    .padding(start = 16.dp, top = 4.dp)
                    .semantics {
                        if (isError) {
                            contentDescription = "Error: $errorText"
                        }
                    }
            )
        }
    }
}

/**
 * Accessible status announcement
 */
@Composable
fun AccessibleStatusAnnouncement(
    message: String,
    type: AnnouncementType = AnnouncementType.NEUTRAL,
    visible: Boolean,
    accessibilitySettings: AccessibilitySettings = AccessibilitySettings()
) {
    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }
    val hapticManager = remember { HapticManager(context) }
    
    LaunchedEffect(visible) {
        if (visible && accessibilitySettings.isScreenReaderEnabled) {
            val prefix = when (type) {
                AnnouncementType.SUCCESS -> "Success: "
                AnnouncementType.ERROR -> "Error: "
                AnnouncementType.WARNING -> "Warning: "
                AnnouncementType.NEUTRAL -> ""
            }
            ttsManager.announce("$prefix$message")
            
            when (type) {
                AnnouncementType.SUCCESS -> hapticManager.success()
                AnnouncementType.ERROR -> hapticManager.error()
                else -> {}
            }
        }
    }
    
    DisposableEffect(ttsManager) {
        onDispose {
            ttsManager.cleanup()
        }
    }
}

enum class AnnouncementType {
    SUCCESS, ERROR, WARNING, NEUTRAL
}

/**
 * Skip links for better navigation
 */
@Composable
fun SkipNavigation(
    onSkipToMain: () -> Unit,
    onSkipToNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(8.dp)
            .semantics {
                contentDescription = "Skip navigation options"
            },
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextButton(
            onClick = onSkipToMain,
            modifier = Modifier.semantics {
                contentDescription = "Skip to main content"
            }
        ) {
            Text("Skip to Main")
        }
        
        TextButton(
            onClick = onSkipToNext,
            modifier = Modifier.semantics {
                contentDescription = "Skip to next step"
            }
        ) {
            Text("Skip to Next")
        }
    }
}

/**
 * Focus indicator for keyboard navigation
 */
@Composable
fun Modifier.accessibilityFocus(
    accessibilitySettings: AccessibilitySettings,
    description: String = ""
) = this.then(
    if (accessibilitySettings.isHighContrastEnabled) {
        Modifier.border(
            width = 2.dp,
            color = AccessibilityColors.HighContrastPrimary,
            shape = RoundedCornerShape(4.dp)
        )
    } else {
        Modifier
    }
).semantics {
    if (description.isNotEmpty()) {
        contentDescription = description
    }
}

/**
 * Reading order helper for complex layouts
 */
@Composable
fun Modifier.readingOrder(order: Int) = this.semantics {
    traversalIndex = order.toFloat()
}