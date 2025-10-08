package com.example.g_kash.core.data


// Data Models
data class AlphaVantageQuote(
    @SerializedName("Global Quote") val globalQuote: GlobalQuote?
)

data class GlobalQuote(
    @SerializedName("01. symbol") val symbol: String,
    @SerializedName("05. price") val price: String,
    @SerializedName("09. change") val change: String,
    @SerializedName("10. change percent") val changePercent: String
)

data class AlphaVantageTimeSeriesDaily(
    @SerializedName("Meta Data") val metaData: MetaData?,
    @SerializedName("Time Series (Daily)") val timeSeries: Map<String, DailyData>?
)

data class MetaData(
    @SerializedName("2. Symbol") val symbol: String,
    @SerializedName("3. Last Refreshed") val lastRefreshed: String
)

data class DailyData(
    @SerializedName("1. open") val open: String,
    @SerializedName("2. high") val high: String,
    @SerializedName("3. low") val low: String,
    @SerializedName("4. close") val close: String,
    @SerializedName("5. volume") val volume: String
)

data class CurrencyExchangeRate(
    @SerializedName("Realtime Currency Exchange Rate") val exchangeRate: ExchangeRateData?
)

data class ExchangeRateData(
    @SerializedName("1. From_Currency Code") val fromCurrency: String,
    @SerializedName("3. To_Currency Code") val toCurrency: String,
    @SerializedName("5. Exchange Rate") val rate: String,
    @SerializedName("6. Last Refreshed") val lastRefreshed: String
)

data class LearningCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
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

// API Service
interface AlphaVantageApi {
    @GET("query")
    suspend fun getQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): AlphaVantageQuote

    @GET("query")
    suspend fun getTimeSeriesDaily(
        @Query("function") function: String = "TIME_SERIES_DAILY",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): AlphaVantageTimeSeriesDaily

    @GET("query")
    suspend fun getCurrencyExchange(
        @Query("function") function: String = "CURRENCY_EXCHANGE_RATE",
        @Query("from_currency") fromCurrency: String,
        @Query("to_currency") toCurrency: String,
        @Query("apikey") apiKey: String
    ): CurrencyExchangeRate
}
