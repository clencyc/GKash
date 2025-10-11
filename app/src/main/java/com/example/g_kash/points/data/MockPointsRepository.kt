package com.example.g_kash.points.data

import com.example.g_kash.points.domain.PointsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class MockPointsRepository : PointsRepository {
    
    private val userPointsMap = mutableMapOf<String, UserPoints>()
    private val learningProgressMap = mutableMapOf<String, MutableList<LearningProgress>>()
    private val stockPurchasesMap = mutableMapOf<String, MutableList<StockPurchase>>()
    private val transactionsMap = mutableMapOf<String, MutableList<PointsTransaction>>()
    
    private val availableStocks = listOf(
        GKashStockOffer(
            id = "gkash_starter",
            stockName = "GKash Starter Pack",
            stockSymbol = "GKSH",
            pointsCost = 100,
            stockValue = 5.0,
            sharesAmount = 0.1,
            description = "Start your investment journey with a fractional share of GKash"
        ),
        GKashStockOffer(
            id = "gkash_basic",
            stockName = "GKash Basic Share",
            stockSymbol = "GKSH",
            pointsCost = 250,
            stockValue = 12.50,
            sharesAmount = 0.25,
            description = "Quarter share of GKash stock - perfect for beginners"
        ),
        GKashStockOffer(
            id = "gkash_premium",
            stockName = "GKash Premium Share",
            stockSymbol = "GKSH",
            pointsCost = 500,
            stockValue = 25.0,
            sharesAmount = 0.5,
            description = "Half share of GKash - for dedicated learners"
        ),
        GKashStockOffer(
            id = "gkash_full",
            stockName = "Full GKash Share",
            stockSymbol = "GKSH",
            pointsCost = 1000,
            stockValue = 50.0,
            sharesAmount = 1.0,
            description = "Complete share of GKash stock - ultimate reward!"
        )
    )
    
    private val learningRewards = listOf(
        LearningReward("module_budgeting_101", "Personal Budgeting Basics", 50, "Budgeting", RewardDifficulty.BEGINNER),
        LearningReward("module_saving_strategies", "Smart Saving Strategies", 75, "Saving", RewardDifficulty.INTERMEDIATE),
        LearningReward("module_investment_intro", "Introduction to Investing", 100, "Investing", RewardDifficulty.INTERMEDIATE),
        LearningReward("module_stock_analysis", "Stock Analysis Fundamentals", 150, "Investing", RewardDifficulty.ADVANCED),
        LearningReward("module_portfolio_mgmt", "Portfolio Management", 200, "Investing", RewardDifficulty.EXPERT)
    )
    
    override suspend fun getUserPoints(userId: String): Result<UserPoints> {
        delay(200) // Simulate network delay
        
        val userPoints = userPointsMap[userId] ?: UserPoints(
            userId = userId,
            totalPoints = 0,
            availablePoints = 0,
            lifetimeEarned = 0,
            lifetimeSpent = 0
        )
        
        userPointsMap[userId] = userPoints
        return Result.success(userPoints)
    }
    
    override fun getUserPointsStream(userId: String): Flow<UserPoints?> = flow {
        while (true) {
            emit(userPointsMap[userId])
            delay(1000)
        }
    }
    
    override suspend fun updateUserPoints(userPoints: UserPoints): Result<Unit> {
        delay(100)
        userPointsMap[userPoints.userId] = userPoints
        return Result.success(Unit)
    }
    
    override suspend fun completeModule(request: CompleteModuleRequest): Result<CompleteModuleResponse> {
        delay(300)
        
        // Check if module already completed
        val userProgress = learningProgressMap[request.userId] ?: mutableListOf()
        val existingProgress = userProgress.find { it.moduleId == request.moduleId }
        
        if (existingProgress?.isCompleted == true) {
            return Result.failure(Exception("Module already completed"))
        }
        
        // Find the reward for this module
        val reward = learningRewards.find { it.moduleId == request.moduleId }
            ?: return Result.failure(Exception("Module not found"))
        
        // Calculate points based on completion score and difficulty
        val basePoints = reward.pointsAwarded
        val scoreMultiplier = request.completionScore ?: 1.0f
        val pointsAwarded = (basePoints * reward.difficulty.multiplier * scoreMultiplier).toInt()
        
        // Update user points
        val currentPoints = userPointsMap[request.userId] ?: UserPoints(
            userId = request.userId,
            totalPoints = 0,
            availablePoints = 0,
            lifetimeEarned = 0,
            lifetimeSpent = 0
        )
        
        val updatedPoints = currentPoints.copy(
            totalPoints = currentPoints.totalPoints + pointsAwarded,
            availablePoints = currentPoints.availablePoints + pointsAwarded,
            lifetimeEarned = currentPoints.lifetimeEarned + pointsAwarded
        )
        
        userPointsMap[request.userId] = updatedPoints
        
        // Update learning progress
        val newProgress = LearningProgress(
            userId = request.userId,
            moduleId = request.moduleId,
            moduleName = reward.moduleName,
            category = reward.category,
            isCompleted = true,
            completedAt = java.time.LocalDateTime.now().toString(),
            pointsEarned = pointsAwarded,
            progressPercentage = 100f
        )
        
        if (existingProgress != null) {
            userProgress.remove(existingProgress)
        }
        userProgress.add(newProgress)
        learningProgressMap[request.userId] = userProgress
        
        // Add transaction record
        val transaction = PointsTransaction(
            id = UUID.randomUUID().toString(),
            userId = request.userId,
            type = TransactionType.EARNED_LEARNING,
            amount = pointsAwarded,
            description = "Completed: ${reward.moduleName}",
            relatedItemId = request.moduleId
        )
        
        val userTransactions = transactionsMap[request.userId] ?: mutableListOf()
        userTransactions.add(transaction)
        transactionsMap[request.userId] = userTransactions
        
        return Result.success(
            CompleteModuleResponse(
                success = true,
                message = "Module completed successfully!",
                pointsAwarded = pointsAwarded,
                newTotalPoints = updatedPoints.totalPoints,
                achievementUnlocked = if (pointsAwarded >= 100) "High Achiever!" else null
            )
        )
    }
    
    override suspend fun getLearningProgress(userId: String): Result<List<LearningProgress>> {
        delay(150)
        val progress = learningProgressMap[userId] ?: emptyList()
        return Result.success(progress)
    }
    
    override suspend fun getAvailableRewards(): Result<List<LearningReward>> {
        delay(100)
        return Result.success(learningRewards)
    }
    
    override suspend fun getAvailableStocks(): Result<List<GKashStockOffer>> {
        delay(150)
        return Result.success(availableStocks)
    }
    
    override suspend fun purchaseStock(request: PurchaseStockRequest): Result<PurchaseStockResponse> {
        delay(400)
        
        val stockOffer = availableStocks.find { it.id == request.stockOfferId }
            ?: return Result.failure(Exception("Stock offer not found"))
        
        val userPoints = userPointsMap[request.userId]
            ?: return Result.failure(Exception("User points not found"))
        
        if (userPoints.availablePoints < stockOffer.pointsCost) {
            return Result.failure(Exception("Insufficient points"))
        }
        
        // Create stock purchase
        val purchase = StockPurchase(
            id = UUID.randomUUID().toString(),
            userId = request.userId,
            stockOfferId = request.stockOfferId,
            stockSymbol = stockOffer.stockSymbol,
            sharesAmount = stockOffer.sharesAmount,
            pointsUsed = stockOffer.pointsCost,
            currentValue = stockOffer.stockValue
        )
        
        // Update user points
        val updatedPoints = userPoints.copy(
            availablePoints = userPoints.availablePoints - stockOffer.pointsCost,
            lifetimeSpent = userPoints.lifetimeSpent + stockOffer.pointsCost
        )
        
        userPointsMap[request.userId] = updatedPoints
        
        // Store purchase
        val userPurchases = stockPurchasesMap[request.userId] ?: mutableListOf()
        userPurchases.add(purchase)
        stockPurchasesMap[request.userId] = userPurchases
        
        // Add transaction record
        val transaction = PointsTransaction(
            id = UUID.randomUUID().toString(),
            userId = request.userId,
            type = TransactionType.SPENT_STOCK_PURCHASE,
            amount = -stockOffer.pointsCost,
            description = "Purchased: ${stockOffer.stockName}",
            relatedItemId = request.stockOfferId
        )
        
        val userTransactions = transactionsMap[request.userId] ?: mutableListOf()
        userTransactions.add(transaction)
        transactionsMap[request.userId] = userTransactions
        
        return Result.success(
            PurchaseStockResponse(
                success = true,
                message = "Stock purchased successfully!",
                purchase = purchase,
                remainingPoints = updatedPoints.availablePoints
            )
        )
    }
    
    override suspend fun getUserStockPurchases(userId: String): Result<List<StockPurchase>> {
        delay(200)
        val purchases = stockPurchasesMap[userId] ?: emptyList()
        return Result.success(purchases)
    }
    
    override suspend fun getPointsHistory(userId: String): Result<PointsHistoryResponse> {
        delay(250)
        
        val transactions = transactionsMap[userId] ?: emptyList()
        val userPoints = userPointsMap[userId] ?: UserPoints(
            userId = userId,
            totalPoints = 0,
            availablePoints = 0,
            lifetimeEarned = 0,
            lifetimeSpent = 0
        )
        
        return Result.success(
            PointsHistoryResponse(
                success = true,
                transactions = transactions.sortedByDescending { it.timestamp },
                currentBalance = userPoints
            )
        )
    }
    
    override suspend fun addPointsTransaction(transaction: PointsTransaction): Result<Unit> {
        delay(100)
        
        val userTransactions = transactionsMap[transaction.userId] ?: mutableListOf()
        userTransactions.add(transaction)
        transactionsMap[transaction.userId] = userTransactions
        
        return Result.success(Unit)
    }
}