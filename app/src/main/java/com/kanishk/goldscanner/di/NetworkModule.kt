package com.kanishk.goldscanner.di

import org.koin.dsl.module
import com.kanishk.goldscanner.data.network.NetworkConfig
import com.kanishk.goldscanner.data.network.service.AuthApiService
import com.kanishk.goldscanner.data.network.service.GoldRateApiService

val networkModule = module {
    single { NetworkConfig(get()) }
    single { get<NetworkConfig>().client }
    single { AuthApiService(get()) }
    single { GoldRateApiService(get<NetworkConfig>()) }
    single { com.kanishk.goldscanner.data.network.service.GoldArticleApiService(get<NetworkConfig>()) }
    single { com.kanishk.goldscanner.data.network.service.CustomerApiService(get<NetworkConfig>()) }
    single { com.kanishk.goldscanner.data.network.service.InvoiceApiService(get<NetworkConfig>()) }
}