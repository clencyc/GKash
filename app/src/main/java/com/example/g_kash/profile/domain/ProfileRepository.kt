package com.example.g_kash.profile.domain

import com.example.g_kash.profile.presentation.model.UserProfile
import com.example.g_kash.profile.presentation.model.UserAchievements

interface ProfileRepository {
    suspend fun getUserProfile(): Result<UserProfile>
    suspend fun getUserAchievements(): Result<UserAchievements>
    suspend fun updateUserProfile(user: UserProfile): Result<Unit>
}
