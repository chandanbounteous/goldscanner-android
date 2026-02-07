package com.kanishk.goldscanner.domain.usecase

import com.kanishk.goldscanner.domain.repository.CustomerRepository
import com.kanishk.goldscanner.data.model.Customer
import com.kanishk.goldscanner.data.model.request.CreateCustomerRequest
import com.goldscanner.data.common.Result

class CreateCustomerUseCase(
    private val customerRepository: CustomerRepository
) {
    suspend operator fun invoke(
        firstName: String,
        lastName: String?,
        phone: String?,
        email: String?
    ): Result<Customer> {
        val request = CreateCustomerRequest(
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            email = email
        )
        return customerRepository.createCustomer(request)
    }
}