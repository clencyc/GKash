package com.example.g_kash.points.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.points.data.*
import com.example.g_kash.points.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// UI States for Points System
data class PointsUiState(
    val userPoints: UserPoints? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class StockStoreUiState(
    val availableStocks: List<GKashStockOffer> = emptyList(),
    val userPurchases: List<StockPurchase> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val purchaseSuccess: String? = null
)

data class LearningProgressUiState(
    val progress: List<LearningProgress> = emptyList(),
    val availableRewards: List<LearningReward> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val completionSuccess: String? = null
)

data class TransactionHistoryUiState(
    val transactions: List<PointsTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// Events
sealed class PointsEvent {
    object NavigateToStore : PointsEvent()
    object NavigateToHistory : PointsEvent()
    data class ShowError(val message: String) : PointsEvent()
    data class ShowSuccess(val message: String) : PointsEvent()
    data class ModuleCompleted(val pointsEarned: Int, val moduleName: String) : PointsEvent()
    data class StockPurchased(val stockName: String, val pointsUsed: Int) : PointsEvent()
}

class PointsViewModel(
    private val getUserPointsUseCase: GetUserPointsUseCase,
    private val completeModuleUseCase: CompleteModuleUseCase,
    private val purchaseStockUseCase: PurchaseStockUseCase,
    private val getAvailableStocksUseCase: GetAvailableStocksUseCase,
    private val getLearningProgressUseCase: GetLearningProgressUseCase,
    private val getPointsHistoryUseCase: GetPointsHistoryUseCase,
    private val getUserStockPurchasesUseCase: GetUserStockPurchasesUseCase
) : ViewModel() {

    private val _pointsUiState = mutableStateOf(PointsUiState())
    val pointsUiState: State<PointsUiState> = _pointsUiState

    private val _stockStoreUiState = mutableStateOf(StockStoreUiState())
    val stockStoreUiState: State<StockStoreUiState> = _stockStoreUiState

    private val _learningProgressUiState = mutableStateOf(LearningProgressUiState())
    val learningProgressUiState: State<LearningProgressUiState> = _learningProgressUiState

    private val _transactionHistoryUiState = mutableStateOf(TransactionHistoryUiState())
    val transactionHistoryUiState: State<TransactionHistoryUiState> = _transactionHistoryUiState

    private val _events = MutableSharedFlow<PointsEvent>()
    val events: SharedFlow<PointsEvent> = _events.asSharedFlow()

    private var currentUserId: String = "demo_user" // This should come from auth system

    init {
        loadUserPoints()
        loadAvailableStocks()
        loadLearningProgress()
    }

    fun setUserId(userId: String) {
        currentUserId = userId
        loadUserPoints()
        loadAvailableStocks()
        loadLearningProgress()
    }

    fun loadUserPoints() {
        viewModelScope.launch {
            _pointsUiState.value = _pointsUiState.value.copy(isLoading = true, error = null)
            
            getUserPointsUseCase(currentUserId).fold(
                onSuccess = { userPoints ->
                    _pointsUiState.value = _pointsUiState.value.copy(
                        userPoints = userPoints,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _pointsUiState.value = _pointsUiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load points"
                    )
                }
            )
        }
    }

    fun completeModule(moduleId: String, completionScore: Float? = null) {
        viewModelScope.launch {
            _learningProgressUiState.value = _learningProgressUiState.value.copy(isLoading = true, error = null)
            
            completeModuleUseCase(currentUserId, moduleId, completionScore).fold(
                onSuccess = { response ->
                    _learningProgressUiState.value = _learningProgressUiState.value.copy(
                        isLoading = false,
                        completionSuccess = response.message
                    )
                    
                    // Emit success event
                    _events.emit(PointsEvent.ModuleCompleted(response.pointsAwarded, moduleId))
                    
                    // Reload points and progress
                    loadUserPoints()
                    loadLearningProgress()
                },
                onFailure = { error ->
                    _learningProgressUiState.value = _learningProgressUiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to complete module"
                    )
                    _events.emit(PointsEvent.ShowError(error.message ?: "Failed to complete module"))
                }
            )
        }
    }

    fun purchaseStock(stockOfferId: String) {
        viewModelScope.launch {
            _stockStoreUiState.value = _stockStoreUiState.value.copy(isLoading = true, error = null)
            
            purchaseStockUseCase(currentUserId, stockOfferId).fold(
                onSuccess = { response ->
                    _stockStoreUiState.value = _stockStoreUiState.value.copy(
                        isLoading = false,
                        purchaseSuccess = response.message
                    )
                    
                    // Emit success event
                    _events.emit(PointsEvent.StockPurchased(
                        response.purchase.stockSymbol,
                        response.purchase.pointsUsed
                    ))
                    
                    // Reload points and purchases
                    loadUserPoints()
                    loadUserStockPurchases()
                },
                onFailure = { error ->
                    _stockStoreUiState.value = _stockStoreUiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to purchase stock"
                    )
                    _events.emit(PointsEvent.ShowError(error.message ?: "Failed to purchase stock"))
                }
            )
        }
    }

    fun loadAvailableStocks() {
        viewModelScope.launch {
            _stockStoreUiState.value = _stockStoreUiState.value.copy(isLoading = true, error = null)
            
            getAvailableStocksUseCase().fold(
                onSuccess = { stocks ->
                    _stockStoreUiState.value = _stockStoreUiState.value.copy(
                        availableStocks = stocks,
                        isLoading = false
                    )
                    // Also load user purchases
                    loadUserStockPurchases()
                },
                onFailure = { error ->
                    _stockStoreUiState.value = _stockStoreUiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load available stocks"
                    )
                }
            )
        }
    }

    private fun loadUserStockPurchases() {
        viewModelScope.launch {
            getUserStockPurchasesUseCase(currentUserId).fold(
                onSuccess = { purchases ->
                    _stockStoreUiState.value = _stockStoreUiState.value.copy(
                        userPurchases = purchases
                    )
                },
                onFailure = { /* Handle silently for now */ }
            )
        }
    }

    fun loadLearningProgress() {
        viewModelScope.launch {
            _learningProgressUiState.value = _learningProgressUiState.value.copy(isLoading = true, error = null)
            
            getLearningProgressUseCase(currentUserId).fold(
                onSuccess = { progress ->
                    _learningProgressUiState.value = _learningProgressUiState.value.copy(
                        progress = progress,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _learningProgressUiState.value = _learningProgressUiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load learning progress"
                    )
                }
            )
        }
    }

    fun loadTransactionHistory() {
        viewModelScope.launch {
            _transactionHistoryUiState.value = _transactionHistoryUiState.value.copy(isLoading = true, error = null)
            
            getPointsHistoryUseCase(currentUserId).fold(
                onSuccess = { response ->
                    _transactionHistoryUiState.value = _transactionHistoryUiState.value.copy(
                        transactions = response.transactions,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _transactionHistoryUiState.value = _transactionHistoryUiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load transaction history"
                    )
                }
            )
        }
    }

    fun clearError() {
        _pointsUiState.value = _pointsUiState.value.copy(error = null)
        _stockStoreUiState.value = _stockStoreUiState.value.copy(error = null, purchaseSuccess = null)
        _learningProgressUiState.value = _learningProgressUiState.value.copy(error = null, completionSuccess = null)
        _transactionHistoryUiState.value = _transactionHistoryUiState.value.copy(error = null)
    }

    // Utility functions for UI
    fun canAffordStock(stock: GKashStockOffer): Boolean {
        val userPoints = _pointsUiState.value.userPoints
        return userPoints != null && userPoints.availablePoints >= stock.pointsCost
    }

    fun getCompletedModulesCount(): Int {
        return _learningProgressUiState.value.progress.count { it.isCompleted }
    }

    fun getTotalPointsEarned(): Int {
        return _pointsUiState.value.userPoints?.lifetimeEarned ?: 0
    }

    fun getTotalStockValue(): Double {
        return _stockStoreUiState.value.userPurchases.sumOf { it.currentValue }
    }
}