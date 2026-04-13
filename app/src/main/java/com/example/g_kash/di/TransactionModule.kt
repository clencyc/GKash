package com.example.g_kash.di

import com.example.g_kash.transactions.data.TransactionRepositoryImpl
import com.example.g_kash.transactions.domain.TransactionRepository
import com.example.g_kash.transactions.presentation.TransactionsViewModel
import com.example.g_kash.wallet.data.BalanceRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val transactionModule = module {
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    viewModel {
        TransactionsViewModel(get(), get<BalanceRepository>())
    }
}