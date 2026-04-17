package com.example.g_kash.payment.di

import com.example.g_kash.payment.data.GkashPaymentApiService
import com.example.g_kash.payment.data.PaymentRepository
import com.example.g_kash.payment.data.PaymentRepositoryImpl
import com.example.g_kash.payment.presentation.PaymentViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val paymentModule = module {
    single { GkashPaymentApiService(get(), get()) }
    
    single<PaymentRepository> { 
        PaymentRepositoryImpl(get()) 
    }
    
    viewModel { (accountId: String) -> 
        PaymentViewModel(get(), get(), accountId) 
    }
}
