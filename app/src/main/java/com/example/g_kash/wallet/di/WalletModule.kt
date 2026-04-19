package com.example.g_kash.wallet.di

import com.example.g_kash.data.SessionStorage
import com.example.g_kash.transactions.domain.TransactionRepository
import com.example.g_kash.wallet.data.WalletRepository
import com.example.g_kash.wallet.data.WalletRepositoryImpl
import com.example.g_kash.wallet.presentation.WalletViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val walletModule = module {
    single<WalletRepository> {
        WalletRepositoryImpl(
            get(), // HttpClient
            get(),  // TransactionRepository
            get()   // BalanceRepository
        )
    }

    viewModel {
        val sessionStorage = get<SessionStorage>()
        // Get the user ID synchronously for the ViewModel initialization
        val userId = runBlocking { sessionStorage.userIdStream.first() } ?: ""

        WalletViewModel(
            userId = userId,
            walletRepository = get(),
            balanceRepository = get(),
            transactionRepository = get(),
            accountsRepository = get()
        )
    }
}
