package com.example.g_kash.core.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TrendingUp
import androidx.lifecycle.ViewModel
import com.example.g_kash.core.data.LearningCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


// ViewModels
class FinancialLearningViewModel : ViewModel() {
    private val _categories = MutableStateFlow<List<LearningCategory>>(emptyList())
    val categories: StateFlow<List<LearningCategory>> = _categories.asStateFlow()

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        _categories.value = listOf(
            LearningCategory("get_started", "Get Started", "Basics of Savings", Icons.Default.Home),
            LearningCategory("saving_basics", "Saving Basics", "Build your wealth", Icons.Default.AccountBalance),
            LearningCategory("investment", "Investment Knowledge", "Increasing wealth", Icons.Default.TrendingUp),
            LearningCategory("security", "Security", "Protect Savings", Icons.Default.Lock)
        )
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }
}

sealed class Screen {
    object Home : Screen()
    object GetStarted : Screen()
    object SavingBasics : Screen()
}
