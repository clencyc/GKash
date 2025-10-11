package com.example.g_kash.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector



enum class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
) {
    GOALS(
        route = "main/goals",
        label = "Goals",
        selectedIcon = Icons.Filled.TrackChanges,
        unselectedIcon = Icons.Outlined.TrackChanges
    ),
    GROUPS(
        route = "main/groups",
        label = "Groups",
        selectedIcon = Icons.Filled.Group,
        unselectedIcon = Icons.Outlined.Group
    ),
    // Investment Simulator will be handled separately as the center button
    LEADERBOARD(
        route = "main/leaderboard",
        label = "LeaderBoard",
        selectedIcon = Icons.Filled.Leaderboard,
        unselectedIcon = Icons.Outlined.Leaderboard
    ),
    PROFILE(
        route = "main/profile",
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}
