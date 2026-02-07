package com.kanishk.goldscanner.data.repository

import com.kanishk.goldscanner.data.network.service.CustomerApiService
import com.kanishk.goldscanner.domain.repository.CustomerRepository
import com.kanishk.goldscanner.data.model.Customer
import com.kanishk.goldscanner.data.model.response.PaginationInfo
import com.goldscanner.data.common.Result
import com.goldscanner.data.common.ErrorResponse
import com.kanishk.goldscanner.data.network.ApiException
import com.kanishk.goldscanner.data.network.AuthenticationException
import android.util.Log

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
}