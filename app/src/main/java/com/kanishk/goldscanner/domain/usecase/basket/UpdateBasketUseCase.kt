package com.kanishk.goldscanner.domain.usecase.basket

import com.kanishk.goldscanner.domain.repository.BasketRepository
import com.kanishk.goldscanner.data.model.request.UpdateBasketRequest
import com.kanishk.goldscanner.data.model.response.Result
import com.kanishk.goldscanner.data.model.response.UpdateBasketResponse

class UpdateBasketUseCase(
    private val basketRepository: BasketRepository
) {
    suspend operator fun invoke(basketId: String, request: UpdateBasketRequest): Result<UpdateBasketResponse> {
        return basketRepository.updateBasket(basketId, request)
    }
}