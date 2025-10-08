package com.example.g_kash.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private val dateFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    // Mock responses for financial questions
    private val financialResponses = mapOf(
        "budget" to "Here's a simple budgeting approach: Follow the 50/30/20 rule - 50% for needs (rent, food), 30% for wants (entertainment), and 20% for savings and debt repayment. Start by tracking your expenses for a month to see where your money goes!",
        "emergency" to "A good emergency fund should cover 3-6 months of your essential expenses. Start small - even $500 can help with unexpected costs. Set up an automatic transfer of $50-100 per month to build this fund gradually.",
        "invest" to "Start investing with these steps: 1) Pay off high-interest debt first, 2) Build your emergency fund, 3) Consider low-cost index funds or ETFs, 4) Start with what you can afford, even $25/month helps, 5) Use tax-advantaged accounts like retirement plans if available.",
        "save" to "Top money-saving tips: 1) Cook at home more often, 2) Cancel unused subscriptions, 3) Use the 24-hour rule for purchases over $100, 4) Buy generic brands, 5) Negotiate bills (phone, insurance), 6) Set up automatic savings transfers.",
        "credit" to "To improve your credit score: 1) Pay bills on time (most important factor), 2) Keep credit utilization below 30%, 3) Don't close old credit cards, 4) Check your credit report for errors, 5) Consider becoming an authorized user on someone else's account.",
        "debt" to "For debt management: 1) List all debts with balances and interest rates, 2) Try the avalanche method (pay minimums on all, extra on highest interest), 3) Consider debt consolidation if it lowers your rate, 4) Create a strict budget to free up money for payments.",
        "retirement" to "For retirement planning: 1) Start as early as possible (compound interest is powerful), 2) Contribute enough to get any employer match, 3) Aim to save 10-15% of income, 4) Use tax-advantaged accounts, 5) Increase contributions when you get raises.",
        "insurance" to "Essential insurance types: 1) Health insurance (critical for medical costs), 2) Auto insurance (if you drive), 3) Renter's/homeowner's insurance, 4) Life insurance (if others depend on you), 5) Disability insurance (protects your income)."
    )
    
    fun sendMessage(message: String) {
        if (message.isBlank()) return
        
        viewModelScope.launch {
            // Add user message
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = message,
                isUser = true,
                timestamp = dateFormatter.format(Date())
            )
            
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + userMessage,
                currentMessage = "",
                isTyping = true
            )
            
            // Simulate AI thinking time
            delay(1000)
            
            // Generate AI response
            val aiResponse = generateAIResponse(message)
            val aiMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = aiResponse,
                isUser = false,
                timestamp = dateFormatter.format(Date())
            )
            
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + aiMessage,
                isTyping = false
            )
        }
    }
    
    fun updateCurrentMessage(message: String) {
        _uiState.value = _uiState.value.copy(currentMessage = message)
    }
    
    private fun generateAIResponse(userMessage: String): String {
        val message = userMessage.lowercase()
        
        return when {
            message.contains("budget") || message.contains("budgeting") -> {
                financialResponses["budget"] ?: getDefaultResponse()
            }
            message.contains("emergency") && message.contains("fund") -> {
                financialResponses["emergency"] ?: getDefaultResponse()
            }
            message.contains("invest") || message.contains("investment") -> {
                financialResponses["invest"] ?: getDefaultResponse()
            }
            message.contains("save") || message.contains("saving") -> {
                financialResponses["save"] ?: getDefaultResponse()
            }
            message.contains("credit") && message.contains("score") -> {
                financialResponses["credit"] ?: getDefaultResponse()
            }
            message.contains("debt") -> {
                financialResponses["debt"] ?: getDefaultResponse()
            }
            message.contains("retirement") -> {
                financialResponses["retirement"] ?: getDefaultResponse()
            }
            message.contains("insurance") -> {
                financialResponses["insurance"] ?: getDefaultResponse()
            }
            message.contains("hello") || message.contains("hi") -> {
                "Hello! I'm your personal finance AI assistant. I'm here to help you with budgeting, saving, investing, debt management, and any other financial questions you might have. What would you like to know?"
            }
            message.contains("thank") -> {
                "You're welcome! I'm always here to help with your financial questions. Remember, small consistent steps lead to big financial improvements over time. Is there anything else you'd like to know?"
            }
            else -> getDefaultResponse()
        }
    }
    
    private fun getDefaultResponse(): String {
        val defaultResponses = listOf(
            "I'd be happy to help you with that financial question! Could you provide more details so I can give you more specific advice?",
            "That's a great question! I can help you with budgeting, saving, investing, debt management, credit scores, and more. What specific area interests you most?",
            "I'm here to help with your personal finance needs! Feel free to ask about budgeting, emergency funds, investing strategies, or any other money-related topics.",
            "Personal finance can seem complex, but I'm here to break it down for you! What aspect of your finances would you like to improve first?",
            "Great question! I specialize in helping with budgeting, saving strategies, investment basics, debt management, and financial planning. How can I assist you today?"
        )
        return defaultResponses.random()
    }
}