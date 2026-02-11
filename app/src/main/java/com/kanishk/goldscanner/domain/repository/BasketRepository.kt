package com.kanishk.goldscanner.domain.repository

import com.kanishk.goldscanner.data.model.Basket
import com.kanishk.goldscanner.data.model.BasketSearchFilter
import com.kanishk.goldscanner.data.model.ActiveBasket
import com.kanishk.goldscanner.utils.Result

interface BasketRepository {
    suspend fun searchBaskets(
        filter: BasketSearchFilter,
        offset: Int,
        limit: Int
    ): Result<Pair<List<Basket>, Boolean>> // Returns (baskets, hasMore)
    
    suspend fun getActiveBasketId(): String?
    suspend fun setActiveBasket(basket: ActiveBasket)
    suspend fun clearActiveBasket()
}