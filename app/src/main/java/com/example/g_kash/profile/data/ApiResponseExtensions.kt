package com.example.g_kash.profile.data

import com.example.g_kash.authentication.data.model.UserProfileResponse
import com.example.g_kash.authentication.data.model.UserAchievementsResponse
import com.example.g_kash.profile.presentation.model.UserProfile
import com.example.g_kash.profile.presentation.model.UserAchievements

// Extension function to convert UserProfileResponse to UserProfile
fun UserProfileResponse.toUserProfile(): UserProfile {
    return UserProfile(
        id = this.id,
        name = this.name,
        email = this.email,
        phoneNumber = this.phoneNumber,
        dateJoined = this.dateJoined
    )
}

// Extension function to convert UserAchievementsResponse to UserAchievements
fun UserAchievementsResponse.toUserAchievements(): UserAchievements {
    return UserAchievements(
        lessonsCompleted = this.lessonsCompleted,
        learningStreak = this.learningStreak,
        savingsGoalsAchieved = this.savingsGoalsAchieved,
        totalTimeSpent = this.totalTimeSpent,
        level = this.level
    )
}
