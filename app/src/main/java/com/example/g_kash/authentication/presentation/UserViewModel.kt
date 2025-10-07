package com.example.g_kash.authentication.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.data.SessionStorage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class UserViewModel(sessionStorage: SessionStorage) : ViewModel() {
    /**
     * A StateFlow that provides the currently logged-in user's ID.
     * It will be null if no user is logged in.
     */
    val userId: StateFlow<String?> = sessionStorage.userIdStream
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}