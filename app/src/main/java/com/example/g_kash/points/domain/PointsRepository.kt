package com.example.g_kash.points.domain

import com.example.g_kash.points.data.*
import kotlinx.coroutines.flow.Flow

// Repository interface for points management
interface PointsRepository {
    
    // User Points Management
    suspend fun getUserPoints(userId: String): Result<UserPoints>
    fun getUserPointsStream(userId: String): Flow<UserPoints?>
    suspend fun updateUserPoints(userPoints: UserPoints): Result<Unit>
    
    // Learning Progress & Rewards
    suspend fun completeModule(request: CompleteModuleRequest): Result<CompleteModuleResponse>
    suspend fun getLearningProgress(userId: String): Result<List<LearningProgress>>
    suspend fun getAvailableRewards(): Result<List<LearningReward>>
    
    // Stock Purchases
    suspend fun getAvailableStocks(): Result<List<GKashStockOffer>>
    suspend fun purchaseStock(request: PurchaseStockRequest): Result<PurchaseStockResponse>
    suspend fun getUserStockPurchases(userId: String): Result<List<StockPurchase>>
    
    // Transaction History
    suspend fun getPointsHistory(userId: String): Result<PointsHistoryResponse>
    suspend fun addPointsTransaction(transaction: PointsTransaction): Result<Unit>
}

// Use Cases for Points System

class GetUserPointsUseCase(private val repository: PointsRepository) {
    suspend operator fun invoke(userId: String): Result<UserPoints> {
        return repository.getUserPoints(userId)
    }
    
    fun getUserPointsFlow(userId: String): Flow<UserPoints?> {
        return repository.getUserPointsStream(userId)
    }
}

class CompleteModuleUseCase(private val repository: PointsRepository) {
    suspend operator fun invoke(
        userId: String,
        moduleId: String,
        completionScore: Float? = null
    ): Result<CompleteModuleResponse> {
        
        // Validate input
        if (userId.isBlank()) {
            return Result.failure(Exception("User ID is required"))
        }
        if (moduleId.isBlank()) {
            return Result.failure(Exception("Module ID is required"))
        }
        
        val request = CompleteModuleRequest(
            userId = userId,
            moduleId = moduleId,
            completionScore = completionScore
        )
        
        return repository.completeModule(request)
    }
}

class PurchaseStockUseCase(private val repository: PointsRepository) {
    suspend operator fun invoke(
        userId: String,
        stockOfferId: String
    ): Result<PurchaseStockResponse> {
        
        // Validate input
        if (userId.isBlank()) {
            return Result.failure(Exception("User ID is required"))
        }
        if (stockOfferId.isBlank()) {
            return Result.failure(Exception("Stock offer ID is required"))
        }
        
        // Check user has enough points
        val userPointsResult = repository.getUserPoints(userId)
        if (userPointsResult.isFailure) {
            return Result.failure(userPointsResult.exceptionOrNull() ?: Exception("Could not fetch user points"))
        }
        
        val userPoints = userPointsResult.getOrNull()!!
        
        // Get stock offer details
        val stocksResult = repository.getAvailableStocks()
        if (stocksResult.isFailure) {
            return Result.failure(Exception("Could not fetch available stocks"))
        }
        
        val stockOffer = stocksResult.getOrNull()!!.find { it.id == stockOfferId }
            ?: return Result.failure(Exception("Stock offer not found"))
        
        if (!stockOffer.isAvailable) {
            return Result.failure(Exception("Stock offer is no longer available"))
        }
        
        if (userPoints.availablePoints < stockOffer.pointsCost) {
            return Result.failure(Exception("Insufficient points. Need ${stockOffer.pointsCost}, have ${userPoints.availablePoints}"))
        }
        
        val request = PurchaseStockRequest(
            userId = userId,
            stockOfferId = stockOfferId
        )
        
        return repository.purchaseStock(request)
    }
}

class GetAvailableStocksUseCase(private val repository: PointsRepository) {
    suspend operator fun invoke(): Result<List<GKashStockOffer>> {
        return repository.getAvailableStocks()
    }
}

class GetLearningProgressUseCase(private val repository: PointsRepository) {
    suspend operator fun invoke(userId: String): Result<List<LearningProgress>> {
        if (userId.isBlank()) {
            return Result.failure(Exception("User ID is required"))
        }
        return repository.getLearningProgress(userId)
    }
}

class GetPointsHistoryUseCase(private val repository: PointsRepository) {
    suspend operator fun invoke(userId: String): Result<PointsHistoryResponse> {
        if (userId.isBlank()) {
            return Result.failure(Exception("User ID is required"))
        }
        return repository.getPointsHistory(userId)
    }
}

class GetUserStockPurchasesUseCase(private val repository: PointsRepository) {
    suspend operator fun invoke(userId: String): Result<List<StockPurchase>> {
        if (userId.isBlank()) {
            return Result.failure(Exception("User ID is required"))
        }
        return repository.getUserStockPurchases(userId)
    }
}