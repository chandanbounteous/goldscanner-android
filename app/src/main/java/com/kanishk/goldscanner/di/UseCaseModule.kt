package com.kanishk.goldscanner.di

import org.koin.dsl.module
import com.kanishk.goldscanner.domain.usecase.auth.LoginUseCase
import com.kanishk.goldscanner.domain.usecase.auth.CheckLoginStatusUseCase

val useCaseModule = module {
    factory { LoginUseCase(get()) }
    factory { CheckLoginStatusUseCase(get()) }
    factory { com.kanishk.goldscanner.domain.usecase.GetCurrentGoldRateUseCase(get()) }
    factory { com.kanishk.goldscanner.domain.usecase.GetGoldArticlesUseCase(get()) }
    factory { com.kanishk.goldscanner.domain.usecase.CreateArticleUseCase(get()) }
    factory { com.kanishk.goldscanner.domain.usecase.UpdateArticleUseCase(get()) }
}