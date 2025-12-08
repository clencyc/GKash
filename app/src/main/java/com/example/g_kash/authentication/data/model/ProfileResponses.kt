package com.example.g_kash.authentication.data.model

data class UserProfileResponse(
    val success: Boolean,
    val message: String? = null,
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val dateJoined: String = ""
)

data class UserAchievementsResponse(
    val success: Boolean,
    val message: String? = null,
    val lessonsCompleted: Int = 0,
    val learningStreak: Int = 0,
    val savingsGoalsAchieved: Int = 0,
    val totalTimeSpent: String = "0m",
    val level: String = "Beginner"
)

data class BaseResponse(
    val success: Boolean,
    val message: String? = null
)
