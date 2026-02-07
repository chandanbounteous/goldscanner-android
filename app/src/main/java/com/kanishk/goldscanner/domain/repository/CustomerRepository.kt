package com.kanishk.goldscanner.domain.repository

import com.kanishk.goldscanner.data.model.Customer
import com.kanishk.goldscanner.data.model.request.CreateCustomerRequest
import com.goldscanner.data.common.Result

interface CustomerRepository {
    suspend fun getCustomerList(
        query: String? = null
    ): Result<List<Customer>>
    
    suspend fun createCustomer(
        request: CreateCustomerRequest
    ): Result<Customer>
}