package com.example.g_kash.profile.presentation.model

data class UserAchievements(
    val lessonsCompleted: Int = 0,
    val learningStreak: Int = 0,
    val savingsGoalsAchieved: Int = 0,
    val totalTimeSpent: String = "0m",
    val level: String = "Beginner"
)
