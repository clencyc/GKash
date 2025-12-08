package com.example.g_kash.profile.data

import com.example.g_kash.profile.domain.ProfileRepository
import com.example.g_kash.profile.presentation.model.UserAchievements
import com.example.g_kash.profile.presentation.model.UserProfile

class ProfileRepositoryImpl constructor(
    private val apiService: com.example.g_kash.authentication.data.ApiService
) : ProfileRepository {
    override suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val userId = "user_123" // Replace with actual user ID
            val response = apiService.getUserProfile(userId)

            // Check if the API call was successful
            if (response.success) {
                Result.success(response.toUserProfile())
            } else {
                Result.failure(Exception(response.message ?: "Failed to load profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserAchievements(): Result<UserAchievements> {
        return try {
            val userId = "user_123" // Replace with actual user ID
            val response = apiService.getUserAchievements(userId)

            if (response.success) {
                Result.success(response.toUserAchievements())
            } else {
                Result.failure(Exception(response.message ?: "Failed to load achievements"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(user: UserProfile): Result<Unit> {
        return try {
            val userId = "user_123" // Replace with actual user ID
            val response = apiService.updateUserProfile(
                id = userId,
                name = user.name,
                email = user.email,
                phoneNumber = user.phoneNumber
            )

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to update profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}