package com.kanishk.goldscanner.domain.repository

import com.kanishk.goldscanner.data.model.Basket
import com.kanishk.goldscanner.data.model.BasketSearchFilter
import com.kanishk.goldscanner.data.model.request.UpdateBasketRequest
import com.kanishk.goldscanner.data.model.response.Result
import com.kanishk.goldscanner.data.model.response.UpdateBasketResponse

interface BasketRepository {
    suspend fun searchBaskets(
        filter: BasketSearchFilter,
        offset: Int,
        limit: Int
    ): Result<Pair<List<Basket>, Boolean>> // Returns (baskets, hasMore)
    
    suspend fun updateBasket(basketId: String, request: UpdateBasketRequest): Result<UpdateBasketResponse>
    suspend fun getActiveBasketId(): String?
    suspend fun setActiveBasketId(basketId: String)
    suspend fun clearActiveBasketId()
}