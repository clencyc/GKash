package com.example.g_kash.wallet.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BalanceRepository(initialBalance: Double = 1000.0) {
    private val _balance = MutableStateFlow(initialBalance)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    fun setBalance(newBalance: Double) {
        _balance.value = newBalance.coerceAtLeast(0.0)
    }

    fun adjustBalance(delta: Double) {
        _balance.update { current -> (current + delta).coerceAtLeast(0.0) }
    }
}
