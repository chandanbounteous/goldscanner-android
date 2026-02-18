package com.kanishk.goldscanner.di

import org.koin.dsl.module
import com.kanishk.goldscanner.domain.usecase.auth.LoginUseCase
import com.kanishk.goldscanner.domain.usecase.auth.CheckLoginStatusUseCase
import com.kanishk.goldscanner.domain.usecase.basket.SearchBasketsUseCase
import com.kanishk.goldscanner.domain.usecase.basket.GetActiveBasketIdUseCase
import com.kanishk.goldscanner.domain.usecase.basket.AddArticleToBasketUseCase
import com.kanishk.goldscanner.domain.usecase.basket.GetBasketDetailsUseCase
import com.kanishk.goldscanner.domain.usecase.basket.UpdateBasketUseCase
import com.kanishk.goldscanner.domain.usecase.basket.DeleteBasketArticleUseCase
import com.kanishk.goldscanner.domain.usecase.basket.UpdateBasketArticleUseCase
import com.kanishk.goldscanner.domain.usecase.ClearActiveBasketIdUseCase
import com.kanishk.goldscanner.domain.usecase.basket.SetActiveBasketIdUseCase

val useCaseModule = module {
    factory { LoginUseCase(get()) }
    factory { CheckLoginStatusUseCase(get()) }
    factory { com.kanishk.goldscanner.domain.usecase.GetCurrentGoldRateUseCase(get()) }
    factory { com.kanishk.goldscanner.domain.usecase.GetGoldArticlesUseCase(get()) }
    factory { com.kanishk.goldscanner.domain.usecase.CreateArticleUseCase(get()) }
    factory { com.kanishk.goldscanner.domain.usecase.UpdateArticleUseCase(get()) }
    factory { com.kanishk.goldscanner.domain.usecase.GetCustomerListUseCase(get()) }
    factory { com.kanishk.goldscanner.domain.usecase.CreateCustomerUseCase(get()) }
    factory { com.kanishk.goldscanner.domain.usecase.CreateBasketUseCase(get(), get(), get()) }
    factory { SearchBasketsUseCase(get()) }
    factory { GetActiveBasketIdUseCase(get()) }
    factory { AddArticleToBasketUseCase(get()) }
    factory { GetBasketDetailsUseCase(get()) }
    factory { UpdateBasketUseCase(get()) }
    factory { DeleteBasketArticleUseCase(get()) }
    factory { UpdateBasketArticleUseCase(get()) }
    factory { ClearActiveBasketIdUseCase(get()) }
    factory { SetActiveBasketIdUseCase(get()) }
}