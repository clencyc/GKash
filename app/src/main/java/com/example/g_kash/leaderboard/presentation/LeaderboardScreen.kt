package com.example.g_kash.leaderboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LeaderboardScreen() {
    val leaderboardData = listOf(
        LeaderboardEntry("Alex Johnson", 12450.50, 1),
        LeaderboardEntry("Maria Garcia", 11230.75, 2),
        LeaderboardEntry("David Chen", 10850.25, 3),
        LeaderboardEntry("Sarah Wilson", 9675.00, 4),
        LeaderboardEntry("Mike Brown", 9234.50, 5),
        LeaderboardEntry("Emma Davis", 8890.25, 6),
        LeaderboardEntry("James Miller", 8456.75, 7),
        LeaderboardEntry("Lisa Anderson", 8012.00, 8),
        LeaderboardEntry("You", 7845.50, 9)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Investment Leaderboard",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Trophy",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Top performers this month",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Top 3 Podium
        TopThreePodium(leaderboardData.take(3))
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Rest of the leaderboard
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(leaderboardData.drop(3)) { index, entry ->
                LeaderboardItem(
                    entry = entry.copy(rank = index + 4),
                    isCurrentUser = entry.name == "You"
                )
            }
        }
    }
}

@Composable
fun TopThreePodium(topThree: List<LeaderboardEntry>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            if (topThree.size > 1) {
                PodiumPosition(topThree[1], 80.dp, Color(0xFFC0C0C0)) // Silver
            }
            if (topThree.isNotEmpty()) {
                PodiumPosition(topThree[0], 100.dp, Color(0xFFFFD700)) // Gold
            }
            if (topThree.size > 2) {
                PodiumPosition(topThree[2], 60.dp, Color(0xFFCD7F32)) // Bronze
            }
        }
    }
}

@Composable
fun PodiumPosition(
    entry: LeaderboardEntry,
    height: Dp,
    medalColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.name.take(2),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = entry.name.split(" ")[0],
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Text(
            text = "$${String.format("%.0f", entry.portfolioValue)}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Podium
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(height)
                .background(
                    color = medalColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#${entry.rank}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = medalColor
            )
        }
    }
}

@Composable
fun LeaderboardItem(
    entry: LeaderboardEntry,
    isCurrentUser: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentUser) 6.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isCurrentUser) 
                            MaterialTheme.colorScheme.secondary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${entry.rank}",
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentUser) 
                        MaterialTheme.colorScheme.onSecondary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCurrentUser) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "You",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = entry.name.take(2),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Name and Portfolio Value
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isCurrentUser) "You" else entry.name,
                    fontSize = 16.sp,
                    fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Medium
                )
                Text(
                    text = "$${String.format("%.2f", entry.portfolioValue)}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Trending indicator
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = "Growing",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

data class LeaderboardEntry(
    val name: String,
    val portfolioValue: Double,
    val rank: Int
)