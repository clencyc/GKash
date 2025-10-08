package com.example.g_kash.core.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import androidx.compose.ui.graphics.vector.ImageVector

// Data Models for Alpha Vantage API
@Serializable
data class AlphaVantageQuote(
    @SerialName("Global Quote") val globalQuote: GlobalQuote?
)

@Serializable
data class GlobalQuote(
    @SerialName("01. symbol") val symbol: String,
    @SerialName("05. price") val price: String,
    @SerialName("09. change") val change: String,
    @SerialName("10. change percent") val changePercent: String
)

@Serializable
data class AlphaVantageTimeSeriesDaily(
    @SerialName("Meta Data") val metaData: MetaData?,
    @SerialName("Time Series (Daily)") val timeSeries: Map<String, DailyData>?
)

@Serializable
data class MetaData(
    @SerialName("2. Symbol") val symbol: String,
    @SerialName("3. Last Refreshed") val lastRefreshed: String
)

@Serializable
data class DailyData(
    @SerialName("1. open") val open: String,
    @SerialName("2. high") val high: String,
    @SerialName("3. low") val low: String,
    @SerialName("4. close") val close: String,
    @SerialName("5. volume") val volume: String
)

@Serializable
data class CurrencyExchangeRate(
    @SerialName("Realtime Currency Exchange Rate") val exchangeRate: ExchangeRateData?
)

@Serializable
data class ExchangeRateData(
    @SerialName("1. From_Currency Code") val fromCurrency: String,
    @SerialName("3. To_Currency Code") val toCurrency: String,
    @SerialName("5. Exchange Rate") val rate: String,
    @SerialName("6. Last Refreshed") val lastRefreshed: String
)

// UI Models
data class LearningCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

data class StockEducationItem(
    val symbol: String,
    val price: String,
    val change: String,
    val changePercent: String,
    val lesson: String
)

data class CurrencyLesson(
    val fromCurrency: String,
    val toCurrency: String,
    val rate: String,
    val lastUpdated: String,
    val educationalNote: String
)

data class FinancialTip(
    val title: String,
    val description: String,
    val category: String
)

data class MarketInsight(
    val title: String,
    val content: String,
    val source: String,
    val timestamp: String
)
