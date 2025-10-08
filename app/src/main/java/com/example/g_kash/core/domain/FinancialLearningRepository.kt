package com.example.g_kash.core.domain

import com.example.g_kash.core.data.*
import kotlinx.coroutines.flow.Flow

interface FinancialLearningRepository {
    suspend fun getStockQuote(symbol: String): Result<AlphaVantageQuote>
    suspend fun getStockTimeSeriesDaily(symbol: String): Result<AlphaVantageTimeSeriesDaily>
    suspend fun getCurrencyExchangeRate(fromCurrency: String, toCurrency: String): Result<CurrencyExchangeRate>
    fun getStockEducationContent(symbols: List<String>): Flow<List<StockEducationItem>>
    fun getCurrencyEducationContent(currencyPairs: List<Pair<String, String>>): Flow<List<CurrencyLesson>>
    fun getFinancialTips(): Flow<List<FinancialTip>>
    fun getMarketInsights(): Flow<List<MarketInsight>>
}

class FinancialLearningRepositoryImpl(
    private val apiService: AlphaVantageApiService
) : FinancialLearningRepository {

    override suspend fun getStockQuote(symbol: String): Result<AlphaVantageQuote> {
        return apiService.getQuote(symbol)
    }

    override suspend fun getStockTimeSeriesDaily(symbol: String): Result<AlphaVantageTimeSeriesDaily> {
        return apiService.getTimeSeriesDaily(symbol)
    }

    override suspend fun getCurrencyExchangeRate(fromCurrency: String, toCurrency: String): Result<CurrencyExchangeRate> {
        return apiService.getCurrencyExchange(fromCurrency, toCurrency)
    }

    override fun getStockEducationContent(symbols: List<String>): Flow<List<StockEducationItem>> {
        return apiService.getStockEducation(symbols)
    }

    override fun getCurrencyEducationContent(currencyPairs: List<Pair<String, String>>): Flow<List<CurrencyLesson>> {
        return apiService.getCurrencyLessons(currencyPairs)
    }

    override fun getFinancialTips(): Flow<List<FinancialTip>> = kotlinx.coroutines.flow.flow {
        // Static financial tips - could be from API or local data
        val tips = listOf(
            FinancialTip(
                title = "Start with just $1 a day",
                description = "Small amounts add up over time. $365 in a year can be your emergency fund start!",
                category = "saving_basics"
            ),
            FinancialTip(
                title = "Emergency Fund First",
                description = "Before investing, build an emergency fund covering 3-6 months of expenses.",
                category = "get_started"
            ),
            FinancialTip(
                title = "Diversify Your Investments",
                description = "Don't put all your eggs in one basket. Spread investments across different assets.",
                category = "investment"
            ),
            FinancialTip(
                title = "Use Strong Passwords",
                description = "Protect your financial accounts with unique, strong passwords and 2FA.",
                category = "security"
            )
        )
        emit(tips)
    }

    override fun getMarketInsights(): Flow<List<MarketInsight>> = kotlinx.coroutines.flow.flow {
        // Mock market insights - in real app, this would come from news API
        val insights = listOf(
            MarketInsight(
                title = "Understanding Market Volatility",
                content = "Market ups and downs are normal. Long-term investing helps smooth out these fluctuations.",
                source = "Alpha Vantage Education",
                timestamp = "2 hours ago"
            ),
            MarketInsight(
                title = "Currency Exchange Basics",
                content = "Exchange rates affect international investments. Understanding USD/KES helps Kenyan savers.",
                source = "Financial Learning Center",
                timestamp = "1 day ago"
            )
        )
        emit(insights)
    }
}