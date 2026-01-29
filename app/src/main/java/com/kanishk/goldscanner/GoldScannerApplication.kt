package com.kanishk.goldscanner

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import com.kanishk.goldscanner.di.*

class GoldScannerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@GoldScannerApplication)
            modules(
                appModule,
                networkModule,
                repositoryModule,
                useCaseModule
            )
        }
    }
}