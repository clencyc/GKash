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
            try {
                // Kenyan-focused financial tips
                val kenyanTips = listOf(
                    FinancialTip(
                        title = "Use Strong Passwords",
                        description = "Always use strong, unique passwords for your M-Pesa and mobile banking apps to protect your money.",
                        category = "Security"
                    ),
                    FinancialTip(
                        title = "Save Before You Spend",
                        description = "Set aside at least 20% of your income in a fixed deposit account before spending on non-essentials.",
                        category = "Savings"
                    ),
                    FinancialTip(
                        title = "Diversify Your Investments",
                        description = "Don't put all your money in one place. Consider NSE stocks, government bonds, and SACCOs for better returns.",
                        category = "Investment"
                    ),
                    FinancialTip(
                        title = "Emergency Fund First",
                        description = "Build an emergency fund of 3-6 months expenses in a money market fund before investing in stocks.",
                        category = "Planning"
                    )
                )
                
                val dailyTip = kenyanTips.randomOrNull()
                _uiState.value = _uiState.value.copy(dailyTip = dailyTip)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load daily tip: ${e.message}"
                )
            }
        }
    }

    private fun loadStockEducation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Kenyan stock market examples with KES prices
                val kenyanStocks = listOf(
                    StockEducationItem(
                        symbol = "EQTY",
                        price = "4,250.00",
                        change = "+125.50",
                        changePercent = "+3.04%",
                        lesson = "Equity Bank is Kenya's leading financial services provider. Understanding banking stocks helps you learn about the financial sector."
                    ),
                    StockEducationItem(
                        symbol = "SCOM",
                        price = "2,850.00",
                        change = "-45.00",
                        changePercent = "-1.55%",
                        lesson = "Safaricom is Kenya's largest telecom company. Technology stocks often show high growth potential but can be volatile."
                    ),
                    StockEducationItem(
                        symbol = "KCB",
                        price = "3,100.00",
                        change = "+78.00",
                        changePercent = "+2.58%",
                        lesson = "KCB Group is a major banking institution. Banking stocks provide exposure to Kenya's economic growth."
                    )
                )
                
                _uiState.value = _uiState.value.copy(
                    stockEducation = kenyanStocks,
                    isLoading = false
                )
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
                // Currency lessons focused on Kenyan Shilling
                val currencyLessons = listOf(
                    CurrencyLesson(
                        fromCurrency = "USD",
                        toCurrency = "KES",
                        rate = "145.50",
                        lastUpdated = "2 minutes ago",
                        educationalNote = "The USD/KES rate affects import costs and inflation. A stronger shilling means cheaper imports but can hurt exporters."
                    ),
                    CurrencyLesson(
                        fromCurrency = "EUR",
                        toCurrency = "KES",
                        rate = "158.75",
                        lastUpdated = "5 minutes ago",
                        educationalNote = "Euro is important for Kenya's trade with Europe. Monitor this rate if you're involved in European trade or travel."
                    ),
                    CurrencyLesson(
                        fromCurrency = "GBP",
                        toCurrency = "KES",
                        rate = "184.20",
                        lastUpdated = "3 minutes ago",
                        educationalNote = "The British Pound historically influences East African currencies. Understanding this helps with investment decisions."
                    )
                )
                
                _uiState.value = _uiState.value.copy(currencyLessons = currencyLessons)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load currency lessons: ${e.message}"
                )
            }
        }
    }

    private fun loadMarketInsights() {
        viewModelScope.launch {
            try {
                // Kenyan market insights
                val insights = listOf(
                    MarketInsight(
                        title = "NSE Performance Update",
                        content = "The Nairobi Securities Exchange 20-Share Index gained 2.1% this week, driven by strong banking sector performance. Equity Bank and KCB led the gains.",
                        source = "NSE Market Report",
                        timestamp = "2 hours ago"
                    ),
                    MarketInsight(
                        title = "KES Strengthens Against USD",
                        content = "The Kenyan Shilling strengthened to KES 145.5 per USD, supported by increased coffee exports and tourism recovery. This is positive for import-dependent sectors.",
                        source = "Central Bank of Kenya",
                        timestamp = "4 hours ago"
                    ),
                    MarketInsight(
                        title = "Mobile Money Growth Continues",
                        content = "M-Pesa transactions grew 15% year-over-year, reaching KES 8.2 trillion in quarterly volume. This drives growth in fintech and banking sectors.",
                        source = "Financial Markets Analysis",
                        timestamp = "1 day ago"
                    )
                )
                
                _uiState.value = _uiState.value.copy(marketInsights = insights)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load market insights: ${e.message}"
                )
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
