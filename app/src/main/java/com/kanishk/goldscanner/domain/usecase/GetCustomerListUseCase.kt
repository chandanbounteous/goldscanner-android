package com.kanishk.goldscanner.domain.usecase

import com.kanishk.goldscanner.domain.repository.CustomerRepository
import com.kanishk.goldscanner.data.model.Customer
import com.kanishk.goldscanner.data.model.response.Result

class GetCustomerListUseCase(
    private val customerRepository: CustomerRepository
) {
    suspend operator fun invoke(
        query: String? = null
    ): Result<List<Customer>> {
        return customerRepository.getCustomerList(query)
    }
}