package com.example.g_kash.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Simulate loading user data
            // In real app, this would come from repository
            val user = UserProfile(
                id = "user_123",
                name = "John Doe",
                email = "john.doe@example.com",
                phoneNumber = "+254 712 345 678",
                dateJoined = "January 2024"
            )
            
            val achievements = UserAchievements(
                lessonsCompleted = 12,
                learningStreak = 5,
                savingsGoalsAchieved = 2,
                totalTimeSpent = "2h 30m",
                level = "Intermediate"
            )
            
            _uiState.value = _uiState.value.copy(
                user = user,
                achievements = achievements,
                isLoading = false
            )
        }
    }
    
    fun updateUserProfile(updatedUser: UserProfile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(user = updatedUser)
            // Save to repository
        }
    }
    
    fun refreshProfile() {
        loadUserProfile()
    }
}