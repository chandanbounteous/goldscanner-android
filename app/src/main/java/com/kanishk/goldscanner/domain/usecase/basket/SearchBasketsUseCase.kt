package com.kanishk.goldscanner.domain.usecase.basket

import com.kanishk.goldscanner.domain.repository.BasketRepository
import com.kanishk.goldscanner.data.model.Basket
import com.kanishk.goldscanner.data.model.BasketSearchFilter
import com.kanishk.goldscanner.data.model.response.Result

class SearchBasketsUseCase(
    private val basketRepository: BasketRepository
) {
    suspend operator fun invoke(
        filter: BasketSearchFilter,
        offset: Int = 0,
        limit: Int = 10
    ): Result<Pair<List<Basket>, Boolean>> {
        return basketRepository.searchBaskets(filter, offset, limit)
    }
}