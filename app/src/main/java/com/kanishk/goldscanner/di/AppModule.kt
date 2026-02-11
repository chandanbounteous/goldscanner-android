package com.kanishk.goldscanner.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.kanishk.goldscanner.utils.LocalStorage
import com.kanishk.goldscanner.presentation.viewmodel.LoginViewModel
import com.kanishk.goldscanner.presentation.viewmodel.SplashViewModel

val appModule = module {
    single { LocalStorage(androidContext()) }
    
    // ViewModels
    viewModel { LoginViewModel(get()) }
    viewModel { SplashViewModel(get()) }
    viewModel { com.kanishk.goldscanner.presentation.viewmodel.GoldRateViewModel(get()) }
    viewModel { com.kanishk.goldscanner.presentation.viewmodel.ArticleListViewModel(get()) }
    viewModel { com.kanishk.goldscanner.presentation.viewmodel.ReactiveArticleDetailViewModel(get(), get(), get(), get(), get()) }
    viewModel { com.kanishk.goldscanner.presentation.viewmodel.CustomerListViewModel(get(), get(), get()) }
    viewModel { com.kanishk.goldscanner.presentation.viewmodel.BasketListViewModel(get(), get()) }
}