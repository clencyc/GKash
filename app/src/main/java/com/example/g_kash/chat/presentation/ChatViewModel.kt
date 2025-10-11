package com.example.g_kash.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.chat.domain.ChatBotRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel(
    private val chatBotRepository: ChatBotRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private val dateFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    
    fun sendMessage(message: String) {
        if (message.isBlank()) return
        
        viewModelScope.launch {
            try {
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
                    isTyping = true,
                    error = null
                )
                
                // Call API to get response
                val result = chatBotRepository.sendMessage(message)
                
                if (result.isSuccess) {
                    val chatResponse = result.getOrThrow()
                    val aiMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        content = chatResponse.response,
                        isUser = false,
                        timestamp = dateFormatter.format(Date())
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + aiMessage,
                        isTyping = false
                    )
                } else {
                    // Handle API error with fallback response
                    val errorMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        content = getFallbackResponse(message),
                        isUser = false,
                        timestamp = dateFormatter.format(Date())
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + errorMessage,
                        isTyping = false,
                        error = "Connection issue - using offline response"
                    )
                }
            } catch (e: Exception) {
                // Handle unexpected errors
                val errorMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    content = "I'm sorry, I'm having trouble connecting right now. Please try again later.",
                    isUser = false,
                    timestamp = dateFormatter.format(Date())
                )
                
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + errorMessage,
                    isTyping = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }
    
    fun updateCurrentMessage(message: String) {
        _uiState.value = _uiState.value.copy(currentMessage = message)
    }
    
    fun resetConversation() {
        viewModelScope.launch {
            try {
                val result = chatBotRepository.resetConversation()
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        messages = emptyList(),
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to reset conversation"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error resetting conversation: ${e.message}"
                )
            }
        }
    }
    
    fun deleteSession() {
        viewModelScope.launch {
            try {
                val result = chatBotRepository.deleteSession()
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        messages = emptyList(),
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to delete session"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error deleting session: ${e.message}"
                )
            }
        }
    }
    
    private fun getFallbackResponse(userMessage: String): String {
        val message = userMessage.lowercase()
        
        // Simple fallback responses when API is unavailable
        return when {
            message.contains("budget") || message.contains("budgeting") -> {
                "Here's a simple budgeting approach: Follow the 50/30/20 rule - 50% for needs (rent, food), 30% for wants (entertainment), and 20% for savings and debt repayment. Start by tracking your expenses for a month to see where your money goes!"
            }
            message.contains("emergency") && message.contains("fund") -> {
                "A good emergency fund should cover 3-6 months of your essential expenses. Start small - even $500 can help with unexpected costs. Set up an automatic transfer of $50-100 per month to build this fund gradually."
            }
            message.contains("invest") || message.contains("investment") -> {
                "Start investing with these steps: 1) Pay off high-interest debt first, 2) Build your emergency fund, 3) Consider low-cost index funds or ETFs, 4) Start with what you can afford, even $25/month helps, 5) Use tax-advantaged accounts like retirement plans if available."
            }
            message.contains("hello") || message.contains("hi") -> {
                "Hello! I'm your personal finance AI assistant. I'm currently running in offline mode, but I can still help with basic financial questions. What would you like to know?"
            }
            message.contains("thank") -> {
                "You're welcome! I'm here to help with your financial questions. Please note I'm currently in offline mode with limited responses."
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