package com.example.g_kash.core.data

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.call.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import com.example.g_kash.core.util.Constants

interface AlphaVantageApiService {
    suspend fun getQuote(symbol: String): Result<AlphaVantageQuote>
    suspend fun getTimeSeriesDaily(symbol: String): Result<AlphaVantageTimeSeriesDaily>
    suspend fun getCurrencyExchange(fromCurrency: String, toCurrency: String): Result<CurrencyExchangeRate>
    fun getStockEducation(symbols: List<String>): Flow<List<StockEducationItem>>
    fun getCurrencyLessons(currencyPairs: List<Pair<String, String>>): Flow<List<CurrencyLesson>>
}

class AlphaVantageApiServiceImpl(
    private val httpClient: HttpClient
) : AlphaVantageApiService {
    
    companion object {
        private val BASE_URL = Constants.ALPHA_VANTAGE_BASE_URL
        private val API_KEY = Constants.ALPHA_VANTAGE_API_KEY
        
        // Educational content for different stocks
        private val stockLessons = mapOf(
            "AAPL" to "Apple Inc. is a technology giant. Understanding tech stocks helps you learn about growth investing and market volatility.",
            "GOOGL" to "Alphabet/Google shows how diversified tech companies work. Learn about revenue streams and digital advertising economics.",
            "MSFT" to "Microsoft demonstrates enterprise software business models. Great for understanding recurring revenue and cloud computing.",
            "TSLA" to "Tesla represents disruptive innovation in automotive and energy. Learn about emerging market investing.",
            "AMZN" to "Amazon shows e-commerce and cloud infrastructure. Perfect for understanding platform business models."
        )
        
        private val currencyEducation = mapOf(
            "USD" to "KES" to "Understanding USD/KES helps Kenyan investors learn about currency exchange and international trade impact on savings.",
            "EUR" to "USD" to "EUR/USD is the world's most traded currency pair. Learn about global economic indicators and central bank policies.",
            "GBP" to "USD" to "GBP/USD shows how Brexit and UK economy affect currency values. Great for understanding political impact on markets."
        )
    }
    
    override suspend fun getQuote(symbol: String): Result<AlphaVantageQuote> {
        return try {
            val response: AlphaVantageQuote = httpClient.get("$BASE_URL/query") {
                parameter("function", "GLOBAL_QUOTE")
                parameter("symbol", symbol)
                parameter("apikey", API_KEY)
            }.body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getTimeSeriesDaily(symbol: String): Result<AlphaVantageTimeSeriesDaily> {
        return try {
            val response: AlphaVantageTimeSeriesDaily = httpClient.get("$BASE_URL/query") {
                parameter("function", "TIME_SERIES_DAILY")
                parameter("symbol", symbol)
                parameter("apikey", API_KEY)
            }.body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCurrencyExchange(fromCurrency: String, toCurrency: String): Result<CurrencyExchangeRate> {
        return try {
            val response: CurrencyExchangeRate = httpClient.get("$BASE_URL/query") {
                parameter("function", "CURRENCY_EXCHANGE_RATE")
                parameter("from_currency", fromCurrency)
                parameter("to_currency", toCurrency)
                parameter("apikey", API_KEY)
            }.body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getStockEducation(symbols: List<String>): Flow<List<StockEducationItem>> = flow {
        val educationItems = mutableListOf<StockEducationItem>()
        
        symbols.forEach { symbol ->
            getQuote(symbol).onSuccess { quote ->
                quote.globalQuote?.let { globalQuote ->
                    educationItems.add(
                        StockEducationItem(
                            symbol = globalQuote.symbol,
                            price = globalQuote.price,
                            change = globalQuote.change,
                            changePercent = globalQuote.changePercent,
                            lesson = stockLessons[symbol] ?: "Learn about this stock and its market behavior."
                        )
                    )
                }
            }
            // Rate limiting - wait between API calls to respect free tier limits
            if (symbols.indexOf(symbol) < symbols.size - 1) {
                delay(Constants.API_CALL_DELAY_MS)
            }
        }
        
        emit(educationItems)
    }
    
    override fun getCurrencyLessons(currencyPairs: List<Pair<String, String>>): Flow<List<CurrencyLesson>> = flow {
        val lessons = mutableListOf<CurrencyLesson>()
        
        currencyPairs.forEach { (from, to) ->
            getCurrencyExchange(from, to).onSuccess { exchangeRate ->
                exchangeRate.exchangeRate?.let { rate ->
                    lessons.add(
                        CurrencyLesson(
                            fromCurrency = rate.fromCurrency,
                            toCurrency = rate.toCurrency,
                            rate = rate.rate,
                            lastUpdated = rate.lastRefreshed,
                            educationalNote = currencyEducation[from to to] 
                                ?: "Understanding currency exchange rates helps in international investment and savings planning."
                        )
                    )
                }
            }
            // Rate limiting - wait between API calls
            if (currencyPairs.indexOf(from to to) < currencyPairs.size - 1) {
                delay(Constants.API_CALL_DELAY_MS)
            }
        }
        
        emit(lessons)
    }
}
