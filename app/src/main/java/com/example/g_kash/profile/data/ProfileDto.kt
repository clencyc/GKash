package com.example.g_kash.profile.data

import com.example.g_kash.profile.presentation.model.UserAchievements
import com.example.g_kash.profile.presentation.model.UserProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the user profile data returned from the GET /user/{id} endpoint.
 * Use @SerialName to map JSON fields (e.g., "phone_number") to Kotlin properties.
 */
@Serializable
data class UserProfileResponse(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("phone_number") // Example if JSON field name is different
    val phoneNumber: String,
    @SerialName("date_joined")
    val dateJoined: String
)

/**
 * Represents the user achievements data from the API.
 * This is an example; adjust fields based on your actual API response.
 */
@Serializable
data class UserAchievementsResponse(
    @SerialName("lessons_completed")
    val lessonsCompleted: Int,
    @SerialName("learning_streak")
    val learningStreak: Int,
    @SerialName("savings_goals_achieved")
    val savingsGoalsAchieved: Int,
    @SerialName("total_time_spent")
    val totalTimeSpent: String,
    val level: String
)

/**
 * A helper function to convert the API response model to the
 * presentation model used by the UI.
 */
fun UserProfileResponse.toUserProfile(): UserProfile {
    return UserProfile(
        id = this.id,
        name = this.name,
        email = this.email,
        phoneNumber = this.phoneNumber,
        dateJoined = this.dateJoined
    )
}

/**
 * A helper function to convert the API response model to the
 * presentation model used by the UI.
 */
fun UserAchievementsResponse.toUserAchievements(): UserAchievements {
    return UserAchievements(
        lessonsCompleted = this.lessonsCompleted,
        learningStreak = this.learningStreak,
        savingsGoalsAchieved = this.savingsGoalsAchieved,
        totalTimeSpent = this.totalTimeSpent,
        level = this.level
    )
}
