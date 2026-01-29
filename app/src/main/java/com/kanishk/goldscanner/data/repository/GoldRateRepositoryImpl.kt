package com.kanishk.goldscanner.data.repository

import com.kanishk.goldscanner.data.network.service.GoldRateApiService
import com.kanishk.goldscanner.domain.repository.GoldRateRepository
import com.kanishk.goldscanner.data.model.response.GoldRateResponse
import com.goldscanner.data.common.Result
import com.goldscanner.data.common.ErrorResponse
import com.kanishk.goldscanner.data.network.ApiException
import com.kanishk.goldscanner.data.network.AuthenticationException

class GoldRateRepositoryImpl(
    private val goldRateApiService: GoldRateApiService
) : GoldRateRepository {
    
    override suspend fun getCurrentGoldRate(): Result<GoldRateResponse> {
        return try {
            val goldRate = goldRateApiService.getCurrentGoldRate()
            Result.Success(goldRate)
        } catch (e: AuthenticationException) {
            // Return authentication error for UI to handle (redirect to login)
            Result.Error(ErrorResponse(
                responseCode = 401,
                responseMessage = "Authentication Required",
                message = e.message ?: "Session expired. Please login again."
            ))
        } catch (e: ApiException.NetworkError) {
            Result.Error(ErrorResponse.networkError(e.message))
        } catch (e: ApiException.ClientError) {
            Result.Error(ErrorResponse(
                responseCode = e.code,
                responseMessage = e.message,
                message = e.body
            ))
        } catch (e: Exception) {
            Result.Error(ErrorResponse.networkError("An unexpected error occurred: ${e.message}"))
        }
    }
}