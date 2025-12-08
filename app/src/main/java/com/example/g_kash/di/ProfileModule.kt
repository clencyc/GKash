package com.example.g_kash.di

import com.example.g_kash.profile.domain.ProfileRepository
import com.example.g_kash.profile.data.ProfileRepositoryImpl
import com.example.g_kash.profile.presentation.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
    single<ProfileRepository> {
        ProfileRepositoryImpl(get())
    }

    viewModel {
        ProfileViewModel(get())
    }
}