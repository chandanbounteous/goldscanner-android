package com.kanishk.goldscanner.domain.repository

import com.kanishk.goldscanner.data.model.Customer
import com.kanishk.goldscanner.data.model.request.CreateCustomerRequest
import com.kanishk.goldscanner.data.model.request.AddArticleToBasketRequest
import com.kanishk.goldscanner.data.model.CreateBasketRequest
import com.kanishk.goldscanner.data.model.CreatedBasket
import com.kanishk.goldscanner.data.model.response.Result
import com.kanishk.goldscanner.data.model.response.AddArticleToBasketResponse
import com.kanishk.goldscanner.data.model.response.BasketDetailResponse

interface CustomerRepository {
    suspend fun getCustomerList(
        query: String? = null
    ): Result<List<Customer>>
    
    suspend fun createCustomer(
        request: CreateCustomerRequest
    ): Result<Customer>
    
    suspend fun createBasket(
        customerId: String, 
        request: CreateBasketRequest
    ): Result<CreatedBasket>
    
    suspend fun addArticleToBasket(
        basketId: String,
        request: AddArticleToBasketRequest
    ): Result<AddArticleToBasketResponse>
    
    suspend fun getBasketDetails(
        basketId: String
    ): Result<BasketDetailResponse>
}