package com.example.g_kash.points.data

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

// Points-related data models

@Serializable
data class UserPoints(
    val userId: String,
    val totalPoints: Int,
    val availablePoints: Int, // Points not yet spent
    val lifetimeEarned: Int,
    val lifetimeSpent: Int,
    val lastUpdated: String = LocalDateTime.now().toString()
)

@Serializable
data class LearningReward(
    val moduleId: String,
    val moduleName: String,
    val pointsAwarded: Int,
    val category: String,
    val difficulty: RewardDifficulty
)

enum class RewardDifficulty(val multiplier: Double) {
    BEGINNER(1.0),
    INTERMEDIATE(1.5),
    ADVANCED(2.0),
    EXPERT(2.5)
}

@Serializable
data class PointsTransaction(
    val id: String,
    val userId: String,
    val type: TransactionType,
    val amount: Int,
    val description: String,
    val relatedItemId: String? = null, // moduleId for earned points, stockId for purchases
    val timestamp: String = LocalDateTime.now().toString()
)

enum class TransactionType {
    EARNED_LEARNING,
    SPENT_STOCK_PURCHASE,
    BONUS_REWARD,
    REFUND
}

@Serializable
data class GKashStockOffer(
    val id: String,
    val stockName: String,
    val stockSymbol: String,
    val pointsCost: Int,
    val stockValue: Double, // Actual monetary value
    val sharesAmount: Double, // How many shares this purchase gets
    val description: String,
    val isAvailable: Boolean = true,
    val limitPerUser: Int? = null // Maximum purchases per user
)

@Serializable
data class StockPurchase(
    val id: String,
    val userId: String,
    val stockOfferId: String,
    val stockSymbol: String,
    val sharesAmount: Double,
    val pointsUsed: Int,
    val purchaseDate: String = LocalDateTime.now().toString(),
    val currentValue: Double // Value at time of purchase
)

@Serializable
data class LearningProgress(
    val userId: String,
    val moduleId: String,
    val moduleName: String,
    val category: String,
    val isCompleted: Boolean,
    val completedAt: String? = null,
    val pointsEarned: Int = 0,
    val progressPercentage: Float = 0f
)

// API Request/Response models
@Serializable
data class CompleteModuleRequest(
    val userId: String,
    val moduleId: String,
    val completionScore: Float? = null
)

@Serializable
data class CompleteModuleResponse(
    val success: Boolean,
    val message: String,
    val pointsAwarded: Int,
    val newTotalPoints: Int,
    val achievementUnlocked: String? = null
)

@Serializable
data class PurchaseStockRequest(
    val userId: String,
    val stockOfferId: String
)

@Serializable
data class PurchaseStockResponse(
    val success: Boolean,
    val message: String,
    val purchase: StockPurchase,
    val remainingPoints: Int
)

@Serializable
data class PointsHistoryResponse(
    val success: Boolean,
    val transactions: List<PointsTransaction>,
    val currentBalance: UserPoints
)