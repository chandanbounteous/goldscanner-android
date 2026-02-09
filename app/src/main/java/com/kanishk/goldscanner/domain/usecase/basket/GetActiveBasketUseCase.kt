package com.kanishk.goldscanner.domain.usecase.basket

import com.kanishk.goldscanner.domain.repository.BasketRepository
import com.kanishk.goldscanner.data.model.ActiveBasket

class GetActiveBasketUseCase(
    private val basketRepository: BasketRepository
) {
    suspend operator fun invoke(): ActiveBasket? {
        return basketRepository.getActiveBasket()
    }
}