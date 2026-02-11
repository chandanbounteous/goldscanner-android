package com.kanishk.goldscanner.domain.repository

import com.kanishk.goldscanner.data.model.Customer
import com.kanishk.goldscanner.data.model.request.CreateCustomerRequest
import com.kanishk.goldscanner.data.model.CreateBasketRequest
import com.kanishk.goldscanner.data.model.CreatedBasket
import com.kanishk.goldscanner.data.model.response.Result

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
}