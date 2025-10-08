package com.example.g_kash.core.util

object Constants {
    // Alpha Vantage API Configuration
    const val ALPHA_VANTAGE_BASE_URL = "https://www.alphavantage.co"
    
    // TODO: Replace with your actual Alpha Vantage API key
    // Get your free API key from: https://www.alphavantage.co/support/#api-key
    const val ALPHA_VANTAGE_API_KEY = "3JR5G7N471PVSXVO"
    
    // Popular stocks for educational content
    val EDUCATIONAL_STOCKS = listOf("AAPL", "GOOGL", "MSFT", "TSLA", "AMZN")
    
    // Currency pairs for educational content
    val EDUCATIONAL_CURRENCY_PAIRS = listOf(
        "USD" to "KES",
        "EUR" to "USD", 
        "GBP" to "USD",
        "USD" to "JPY"
    )
    
    // API call limits (Alpha Vantage free tier: 5 calls per minute, 500 per day)
    const val API_CALL_DELAY_MS = 12000L // 12 seconds between calls
}