package com.kanishk.goldscanner.data.repository

import com.kanishk.goldscanner.data.network.service.GoldRateApiService
import com.kanishk.goldscanner.domain.repository.GoldRateRepository
import com.kanishk.goldscanner.data.model.response.GoldRateResponse
import com.goldscanner.data.common.Result
import com.goldscanner.data.common.ErrorResponse
import com.kanishk.goldscanner.data.network.ApiException
import com.kanishk.goldscanner.data.network.AuthenticationException
import com.kanishk.goldscanner.utils.LocalStorage
import com.kanishk.goldscanner.utils.NepaliDateUtils

class GoldRateRepositoryImpl(
    private val goldRateApiService: GoldRateApiService,
    private val localStorage: LocalStorage
) : GoldRateRepository {
    
    override suspend fun getCurrentGoldRate(): Result<GoldRateResponse> {
        return try {
            // Get cached gold rate info from local storage
            val cachedGoldRate = localStorage.getObject<GoldRateResponse>(LocalStorage.StorageKey.CURRENT_GOLD_RATE_INFO)
            
            if (cachedGoldRate == null) {
                // No cached data, fetch from API
                return fetchAndCacheGoldRate()
            }
            
            // Get the cached Nepali date from the cached gold rate
            val cachedNepaliDate = cachedGoldRate.date
            
            // Get current Nepali date
            val currentNepaliDate = NepaliDateUtils.getCurrentNepaliDate()
            
            // If current date is later than cached date, fetch fresh data
            if (NepaliDateUtils.isAfter(currentNepaliDate, cachedNepaliDate)) {
                fetchAndCacheGoldRate()
            } else {
                // Current date is same or before cached date, return cached data
                Result.Success(cachedGoldRate)
            }
        } catch (e: Exception) {
            Result.Error(ErrorResponse.networkError("An unexpected error occurred: ${e.message}"))
        }
    }
    
    private suspend fun fetchAndCacheGoldRate(): Result<GoldRateResponse> {
        return try {
            val goldRate = goldRateApiService.getCurrentGoldRate()
            // Store the response in local storage
            localStorage.saveObject(LocalStorage.StorageKey.CURRENT_GOLD_RATE_INFO, goldRate)
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