package com.kanishk.goldscanner.di

import org.koin.dsl.module
import com.kanishk.goldscanner.data.repository.AuthRepositoryImpl
import com.kanishk.goldscanner.domain.repository.AuthRepository

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single<com.kanishk.goldscanner.domain.repository.GoldRateRepository> { com.kanishk.goldscanner.data.repository.GoldRateRepositoryImpl(get()) }
}