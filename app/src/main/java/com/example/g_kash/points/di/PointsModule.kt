package com.example.g_kash.points.di

import com.example.g_kash.points.data.MockPointsRepository
import com.example.g_kash.points.domain.*
import com.example.g_kash.points.presentation.PointsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val pointsModule = module {
    // Repository
    single { MockPointsRepository() }
    
    // Use Cases
    single { GetUserPointsUseCase(get()) }
    single { CompleteModuleUseCase(get()) }
    single { PurchaseStockUseCase(get()) }
    single { GetAvailableStocksUseCase(get()) }
    single { GetLearningProgressUseCase(get()) }
    single { GetPointsHistoryUseCase(get()) }
    single { GetUserStockPurchasesUseCase(get()) }
    
    // ViewModel
    viewModel { 
        PointsViewModel(
            getUserPointsUseCase = get(),
            completeModuleUseCase = get(),
            purchaseStockUseCase = get(),
            getAvailableStocksUseCase = get(),
            getLearningProgressUseCase = get(),
            getPointsHistoryUseCase = get(),
            getUserStockPurchasesUseCase = get()
        )
    }
}
