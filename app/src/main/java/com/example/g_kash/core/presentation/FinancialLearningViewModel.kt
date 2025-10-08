package com.example.g_kash.core.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TrendingUp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.core.data.*
import com.example.g_kash.core.domain.FinancialLearningRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LearnScreenUiState(
    val isLoading: Boolean = false,
    val categories: List<LearningCategory> = emptyList(),
    val dailyTip: FinancialTip? = null,
    val stockEducation: List<StockEducationItem> = emptyList(),
    val currencyLessons: List<CurrencyLesson> = emptyList(),
    val marketInsights: List<MarketInsight> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = ""
)

class FinancialLearningViewModel(
    private val repository: FinancialLearningRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LearnScreenUiState())
    val uiState: StateFlow<LearnScreenUiState> = _uiState.asStateFlow()

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        loadCategories()
        loadDailyTip()
        loadStockEducation()
        loadCurrencyLessons()
        loadMarketInsights()
    }

    private fun loadCategories() {
        val categories = listOf(
            LearningCategory("get_started", "Get Started", "Basics of Savings", Icons.Default.Home),
            LearningCategory("saving_basics", "Saving Basics", "Build your wealth", Icons.Default.AccountBalance),
            LearningCategory("investment", "Investment Knowledge", "Increasing wealth", Icons.Default.TrendingUp),
            LearningCategory("security", "Security", "Protect Savings", Icons.Default.Lock)
        )
        _uiState.value = _uiState.value.copy(categories = categories)
    }

    private fun loadDailyTip() {
        viewModelScope.launch {
            repository.getFinancialTips().collect { tips ->
                val dailyTip = tips.randomOrNull()
                _uiState.value = _uiState.value.copy(dailyTip = dailyTip)
            }
        }
    }

    private fun loadStockEducation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val popularStocks = listOf("AAPL", "GOOGL", "MSFT")
                repository.getStockEducationContent(popularStocks).collect { stockItems ->
                    _uiState.value = _uiState.value.copy(
                        stockEducation = stockItems,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load stock education: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun loadCurrencyLessons() {
        viewModelScope.launch {
            try {
                val currencyPairs = listOf(
                    "USD" to "KES",
                    "EUR" to "USD",
                    "GBP" to "USD"
                )
                repository.getCurrencyEducationContent(currencyPairs).collect { lessons ->
                    _uiState.value = _uiState.value.copy(currencyLessons = lessons)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load currency lessons: ${e.message}"
                )
            }
        }
    }

    private fun loadMarketInsights() {
        viewModelScope.launch {
            repository.getMarketInsights().collect { insights ->
                _uiState.value = _uiState.value.copy(marketInsights = insights)
            }
        }
    }

    fun refreshData() {
        loadInitialData()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun loadSpecificStock(symbol: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.getStockEducationContent(listOf(symbol)).collect { stockItems ->
                    val currentStocks = _uiState.value.stockEducation.toMutableList()
                    stockItems.forEach { newItem ->
                        // Replace existing or add new
                        val existingIndex = currentStocks.indexOfFirst { it.symbol == newItem.symbol }
                        if (existingIndex >= 0) {
                            currentStocks[existingIndex] = newItem
                        } else {
                            currentStocks.add(newItem)
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        stockEducation = currentStocks,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load stock $symbol: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

sealed class Screen {
    object Home : Screen()
    object GetStarted : Screen()
    object SavingBasics : Screen()
}
