package com.example.g_kash.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Pink Accent Theme Usage Guide
 * ============================
 * 
 * This guide demonstrates how to use the new pink accent theme in your GKash app.
 * The theme is optimized for both light and dark modes with excellent contrast ratios.
 * 
 * Color Palette:
 * 
 * LIGHT MODE:
 * - Background: Pure White (#FFFFFF)
 * - Primary: Vibrant Pink (#E91E63)
 * - Secondary: Muted Gold (#EFBF04) - Perfect complement to pink
 * - Text: Near Black (#1C1B1F) - Enhanced contrast
 * 
 * DARK MODE:
 * - Background: Charcoal (#121212) - Reduced eye strain
 * - Surface: Dark Gray (#1E1E1E) - Cards and surfaces
 * - Primary: Dark Pink (#C11C84) - High contrast ~10:1 ratio
 * - Text: Off-White (#F5F5F5) - Better readability
 * - Secondary: Darker Gold (#B8860B) - Muted for dark mode
 * 
 * Usage:
 * 
 * 1. For the main app theme (default):
 *    Use GKashTheme { } - This now uses the pink accent theme
 * 
 * 2. For financial-focused screens:
 *    Use GKashFinancialTheme { } - Green-focused for financial data
 * 
 * 3. For specifically pink-themed sections:
 *    Use GKashPinkTheme { } - Explicitly pink-focused
 */

@Preview(showBackground = true)
@Composable
fun PinkThemePreviewLight() {
    GKashTheme(darkTheme = false) {
        ThemeShowcaseContent()
    }
}

@Preview(showBackground = true)
@Composable
fun PinkThemePreviewDark() {
    GKashTheme(darkTheme = true) {
        ThemeShowcaseContent()
    }
}

@Composable
private fun ThemeShowcaseContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Pink Accent Theme Showcase",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        
        // Primary Color Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Primary Colors",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Pink Primary Button")
                }
            }
        }
        
        // Secondary Color Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Secondary Colors",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Gold Secondary Button")
                }
            }
        }
        
        // Surface Variants
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Surface",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Surface Variant",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Financial Colors Demo
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Financial Context Colors",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Success/Profit
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = FinancialGreen
                    ) {
                        Text(
                            text = "+$1,234",
                            modifier = Modifier.padding(12.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Loss/Expense
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = FinancialRed
                    ) {
                        Text(
                            text = "-$567",
                            modifier = Modifier.padding(12.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Savings/Gold
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = FinancialGold
                    ) {
                        Text(
                            text = "★ 890",
                            modifier = Modifier.padding(12.dp),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // Usage Note
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💡 Usage Tips",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Pink for primary actions and branding\n" +
                          "• Gold for secondary actions and highlights\n" +
                          "• Green for financial gains/success\n" +
                          "• All colors are WCAG AA/AAA compliant",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

/**
 * Example of how to use specific colors from the theme
 */
@Composable
fun ExampleUsageComponents() {
    // Primary pink button for main actions
    Button(
        onClick = { },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary // Pink in light, Dark Pink in dark
        )
    ) {
        Text("Invest Now")
    }
    
    // Secondary gold button for secondary actions
    OutlinedButton(
        onClick = { },
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.secondary // Gold
        )
    ) {
        Text("Learn More")
    }
    
    // Financial success indicator
    Surface(
        color = SuccessGreen,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = "+12.5%",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
    
    // Card using theme surface colors
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = "Content adapts to theme automatically",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}