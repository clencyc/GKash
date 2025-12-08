package com.example.g_kash.profile.presentation

import com.example.g_kash.profile.presentation.model.UserProfile
import com.example.g_kash.profile.presentation.model.UserAchievements

data class ProfileUiState(
    val user: UserProfile = UserProfile(),
    val achievements: UserAchievements = UserAchievements(),
    val isLoading: Boolean = false,
    val error: String? = null
)
