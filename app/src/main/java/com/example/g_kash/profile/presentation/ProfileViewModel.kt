package com.example.g_kash.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.profile.domain.ProfileRepository
import com.example.g_kash.profile.presentation.model.UserProfile
import com.example.g_kash.profile.presentation.model.UserAchievements
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.component.KoinComponent

class ProfileViewModel(
    private val repository: ProfileRepository
) : ViewModel(), KoinComponent {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Load user profile and achievements in parallel
            val profileResult = repository.getUserProfile()
            val achievementsResult = repository.getUserAchievements()
            
            _uiState.value = _uiState.value.copy(
                user = profileResult.getOrNull() ?: UserProfile(),
                achievements = achievementsResult.getOrNull() ?: UserAchievements(),
                isLoading = false,
                error = listOfNotNull(
                    profileResult.exceptionOrNull()?.message,
                    achievementsResult.exceptionOrNull()?.message
                ).joinToString("\n").takeIf { it.isNotEmpty() }
            )
        }
    }
    
    fun updateUserProfile(updatedUser: UserProfile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            repository.updateUserProfile(updatedUser)
                .onSuccess {
                    // Refresh the profile data
                    loadUserProfile()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to update profile",
                        isLoading = false
                    )
                }
            _uiState.value = _uiState.value.copy(user = updatedUser)
            // Save to repository
        }
    }
    
    fun refreshProfile() {
        loadUserProfile()
    }
}