package com.kanishk.goldscanner.data.repository

import com.kanishk.goldscanner.data.network.service.GoldRateApiService
import com.kanishk.goldscanner.domain.repository.GoldRateRepository
import com.kanishk.goldscanner.data.model.response.GoldRateResponse
import com.goldscanner.data.common.Result
import com.goldscanner.data.common.ErrorResponse
import com.kanishk.goldscanner.data.network.ApiException
import com.kanishk.goldscanner.data.network.AuthenticationException
import com.kanishk.goldscanner.utils.LocalStorage
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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
            
            // Check current Nepali time
            val currentNepaliTime = getCurrentNepaliTime()
            val updateHour = getGoldRateUpdateHour()
            
            // If current time is on or after update hour (default 12 PM), return cached data
            if (currentNepaliTime.hour >= updateHour) {
                Result.Success(cachedGoldRate)
            } else {
                // Before update hour, fetch fresh data
                fetchAndCacheGoldRate()
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
    
    private fun getCurrentNepaliTime(): ZonedDateTime {
        // Nepal timezone is UTC+05:45
        val nepaliZone = ZoneId.of("Asia/Kathmandu")
        return ZonedDateTime.now(nepaliZone)
    }
    
    private fun getGoldRateUpdateHour(): Int {
        // Get configurable update hour, default to 12 (12 PM)
        return localStorage.getValue<Int>(LocalStorage.StorageKey.GOLD_RATE_UPDATE_HOUR) ?: 12
    }
    
    // Method to set the configurable update hour
    fun setGoldRateUpdateHour(hour: Int) {
        localStorage.save(LocalStorage.StorageKey.GOLD_RATE_UPDATE_HOUR, hour)
    }
}