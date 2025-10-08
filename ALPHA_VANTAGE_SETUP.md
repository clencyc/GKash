# Alpha Advantage API Setup Guide

## Getting Started

### 1. Get Your API Key
1. Visit [Alpha Vantage](https://www.alphavantage.co/support/#api-key)
2. Sign up for a free account
3. Get your API key (free tier allows 5 API calls per minute, 500 per day)

### 2. Configure Your API Key
Replace `YOUR_API_KEY_HERE` in the following file:
```kotlin
// app/src/main/java/com/example/g_kash/core/util/Constants.kt
const val ALPHA_VANTAGE_API_KEY = "YOUR_ACTUAL_API_KEY_HERE"
```

### 3. What's Been Set Up

#### Architecture:
- **Data Layer**: `AlphaVantageApiService` for API calls
- **Domain Layer**: `FinancialLearningRepository` for business logic
- **Presentation Layer**: `FinancialLearningViewModel` with comprehensive UI state
- **UI Layer**: Beautiful Learn Screen matching your design

#### Features Implemented:
- **Daily Financial Tips**: Rotates through educational tips
- **Learning Categories**: Get Started, Saving Basics, Investment Knowledge, Security
- **Stock Education**: Real-time stock data with educational lessons
- **Currency Education**: Exchange rates with learning notes (especially USD/KES for Kenyan users)
- **Market Insights**: Educational content about market behavior
- **Search Functionality**: Search through financial topics
- **Error Handling**: Comprehensive error states
- **Loading States**: Proper loading indicators
- **Rate Limiting**: Respects API limits automatically

#### API Endpoints Used:
- `GLOBAL_QUOTE`: Get real-time stock prices
- `TIME_SERIES_DAILY`: Historical stock data
- `CURRENCY_EXCHANGE_RATE`: Currency exchange rates

### 4. Koin Dependency Injection
Everything is properly set up in your existing DI modules:
- `AlphaVantageApiService` with separate HTTP client
- `FinancialLearningRepository` 
- Updated `FinancialLearningViewModel` with repository injection

### 5. UI Components Created:
- `LearnHeader()` - "Learn & grow your Savings" header
- `SearchBar()` - Search functionality
- `DailyTipCard()` - Yellow tip card matching your design
- `LearningCategoryItem()` - Category rows with icons
- `StockEducationCard()` - Horizontal scrolling stock cards
- `CurrencyLessonCard()` - Currency education cards
- `MarketInsightCard()` - Market insights

### 6. Educational Content:
The app includes curated educational content for:
- **Stocks**: Apple, Google, Microsoft, Tesla, Amazon with investment lessons
- **Currencies**: USD/KES (important for Kenyan users), EUR/USD, GBP/USD
- **Financial Tips**: Emergency funds, diversification, security, saving basics

### 7. Error Handling & Rate Limiting:
- Automatic 12-second delays between API calls to respect free tier
- Comprehensive error handling with user-friendly messages
- Loading states for better UX

### 8. Next Steps:
1. Add your Alpha Vantage API key
2. Test the API integration
3. Customize educational content as needed
4. Add more learning categories if desired
5. Consider upgrading to Alpha Vantage premium for higher API limits

### 9. Free Tier Limitations:
- 5 API calls per minute
- 500 API calls per day
- No real-time data (15-20 minute delay)

The app is designed to work well within these limits with automatic rate limiting and educational content that doesn't require constant API calls.