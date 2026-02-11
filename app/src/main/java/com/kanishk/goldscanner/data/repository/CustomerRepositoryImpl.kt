package com.kanishk.goldscanner.data.repository

import com.kanishk.goldscanner.data.network.service.CustomerApiService
import com.kanishk.goldscanner.domain.repository.CustomerRepository
import com.kanishk.goldscanner.data.model.Customer
import com.kanishk.goldscanner.data.model.request.CreateCustomerRequest
import com.kanishk.goldscanner.data.model.response.PaginationInfo
import com.goldscanner.data.common.Result
import com.goldscanner.data.common.ErrorResponse
import com.kanishk.goldscanner.data.network.ApiException
import com.kanishk.goldscanner.data.network.AuthenticationException
import android.util.Log
import com.kanishk.goldscanner.data.model.CreateBasketRequest
import com.kanishk.goldscanner.data.model.CreatedBasket

class CustomerRepositoryImpl(
    private val customerApiService: CustomerApiService
) : CustomerRepository {
    
    override suspend fun getCustomerList(
        query: String?
    ): Result<List<Customer>> {
        return try {
            val response = customerApiService.getCustomerList(query)
            val customers = response.body.customers
            Result.Success(customers)
        } catch (e: AuthenticationException) {
            Result.Error(ErrorResponse.authenticationError(e.message ?: "Authentication failed"))
        } catch (e: ApiException.ClientError) {
            Result.Error(ErrorResponse.clientError(e.code, e.message, e.body))
        } catch (e: ApiException.NetworkError) {
            Result.Error(ErrorResponse.networkError(e.message))
        } catch (e: Exception) {
            Log.e("CustomerRepository", "Failed to get customer list", e)
            Result.Error(ErrorResponse.networkError("An unexpected error occurred: ${e.message}"))
        }
    }
    
    override suspend fun createCustomer(
        request: CreateCustomerRequest
    ): Result<Customer> {
        return try {
            val response = customerApiService.createCustomer(request)
            val customer = response.body.customer
            Result.Success(customer)
        } catch (e: AuthenticationException) {
            Result.Error(ErrorResponse.authenticationError(e.message ?: "Authentication failed"))
        } catch (e: ApiException.ClientError) {
            Result.Error(ErrorResponse.clientError(e.code, e.message, e.body))
        } catch (e: ApiException.NetworkError) {
            Result.Error(ErrorResponse.networkError(e.message))
        } catch (e: Exception) {
            Log.e("CustomerRepository", "Failed to create customer", e)
            Result.Error(ErrorResponse.networkError("An unexpected error occurred: ${e.message}"))
        }
    }
    
    override suspend fun createBasket(
        customerId: String,
        request: CreateBasketRequest
    ): Result<CreatedBasket> {
        return try {
            val response = customerApiService.createBasket(customerId, request)
            val basket = response.body.basket
            Result.Success(basket)
        } catch (e: AuthenticationException) {
            Result.Error(ErrorResponse.authenticationError(e.message ?: "Authentication failed"))
        } catch (e: ApiException.ClientError) {
            Result.Error(ErrorResponse.clientError(e.code, e.message, e.body))
        } catch (e: ApiException.NetworkError) {
            Result.Error(ErrorResponse.networkError(e.message))
        } catch (e: Exception) {
            Log.e("CustomerRepository", "Failed to create basket", e)
            Result.Error(ErrorResponse.networkError("An unexpected error occurred: ${e.message}"))
        }
    }
}