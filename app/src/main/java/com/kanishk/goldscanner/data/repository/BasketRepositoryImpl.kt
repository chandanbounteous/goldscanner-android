package com.kanishk.goldscanner.data.repository

import com.kanishk.goldscanner.domain.repository.BasketRepository
import com.kanishk.goldscanner.data.model.Basket
import com.kanishk.goldscanner.data.model.BasketSearchFilter
import com.kanishk.goldscanner.data.model.request.BasketSearchRequest
import com.kanishk.goldscanner.data.model.request.UpdateBasketRequest
import com.kanishk.goldscanner.data.model.response.BasketItem
import com.kanishk.goldscanner.data.model.response.NepaliDate
import com.kanishk.goldscanner.data.model.response.ErrorResponse
import com.kanishk.goldscanner.data.model.response.UpdateBasketResponse
import com.kanishk.goldscanner.data.network.service.CustomerApiService
import com.kanishk.goldscanner.data.network.ApiException
import com.kanishk.goldscanner.data.network.AuthenticationException
import com.kanishk.goldscanner.utils.LocalStorage
import com.kanishk.goldscanner.data.model.response.Result

class BasketRepositoryImpl(
    private val customerApiService: CustomerApiService,
    private val localStorage: LocalStorage
) : BasketRepository {

    override suspend fun searchBaskets(
        filter: BasketSearchFilter,
        offset: Int,
        limit: Int
    ): Result<Pair<List<Basket>, Boolean>> {
        return try {
            val request = BasketSearchRequest(
                customerName = filter.customerName?.takeIf { it.isNotBlank() },
                phone = filter.phone?.takeIf { it.isNotBlank() },
                startDate = filter.startDate?.let { convertUIDateToNepaliFormat(it) },
                endDate = filter.endDate?.let { convertUIDateToNepaliFormat(it) },
                includeBilled = filter.includeBilled,
                includeDiscarded = filter.includeDiscarded,
                offset = offset,
                limit = limit
            )

            val response = customerApiService.searchBaskets(request)
            val baskets = response.body.baskets.map { basketItem ->
                mapBasketItemToDomain(basketItem)
            }

            Result.Success(Pair(baskets, response.body.pagination.hasMore))
        } catch (e: AuthenticationException) {
            Result.Error(ErrorResponse(responseMessage = e.message ?: "Authentication failed", message = e.message ?: "Authentication failed"))
        } catch (e: ApiException.ServerError) {
            Result.Error(ErrorResponse(responseMessage = e.message ?: "Server error occurred", message = e.message ?: "Server error occurred"))
        } catch (e: ApiException.NetworkError) {
            Result.Error(ErrorResponse(responseMessage = e.message ?: "Network error occurred", message = e.message ?: "Network error occurred"))
        } catch (e: Exception) {
            Result.Error(ErrorResponse(responseMessage = "Unexpected error: ${e.message}", message = "Unexpected error: ${e.message}"))
        }
    }
    
    override suspend fun updateBasket(basketId: String, request: UpdateBasketRequest): Result<UpdateBasketResponse> {
        return try {
            val response = customerApiService.updateBasket(basketId, request)
            Result.Success(response)
        } catch (e: AuthenticationException) {
            Result.Error(ErrorResponse(responseMessage = e.message ?: "Authentication failed", message = e.message ?: "Authentication failed"))
        } catch (e: ApiException.ServerError) {
            Result.Error(ErrorResponse(responseMessage = e.message ?: "Server error occurred", message = e.message ?: "Server error occurred"))
        } catch (e: ApiException.ClientError) {
            Result.Error(ErrorResponse(responseMessage = e.message ?: "Client error occurred", message = e.message ?: "Client error occurred"))
        } catch (e: ApiException.NetworkError) {
            Result.Error(ErrorResponse(responseMessage = e.message ?: "Network error occurred", message = e.message ?: "Network error occurred"))
        } catch (e: Exception) {
            Result.Error(ErrorResponse(responseMessage = "Unexpected error: ${e.message}", message = "Unexpected error: ${e.message}"))
        }
    }

    override suspend fun getActiveBasketId(): String? {
        return localStorage.getValue<String>(LocalStorage.StorageKey.ACTIVE_BASKET_ID)
    }

    override suspend fun setActiveBasketId(basketId: String) {
        localStorage.save(LocalStorage.StorageKey.ACTIVE_BASKET_ID, basketId)
    }

    override suspend fun clearActiveBasketId() {
        localStorage.remove(LocalStorage.StorageKey.ACTIVE_BASKET_ID)
    }

    private fun mapBasketItemToDomain(basketItem: BasketItem): Basket {
        return Basket(
            id = basketItem.id,
            basketNumber = basketItem.basketNumber,
            date = basketItem.date,
            nepaliDateFormatted = formatNepaliDate(basketItem.nepaliDate),
            customerName = "${basketItem.firstName} ${basketItem.lastName ?: ""}".trim(),
            phone = basketItem.phone,
            articleCount = basketItem.count,
            isBilled = basketItem.isBilled,
            isDiscarded = basketItem.isDiscarded ?: false
        )
    }

    private fun formatNepaliDate(nepaliDate: NepaliDate): String {
        val monthNames = arrayOf(
            "Baisakh", "Jestha", "Ashadh", "Shrawan", "Bhadra", "Ashwin",
            "Kartik", "Mangsir", "Poush", "Magh", "Falgun", "Chaitra"
        )
        
        val monthName = if (nepaliDate.month in 1..12) {
            monthNames[nepaliDate.month - 1]
        } else {
            "Month${nepaliDate.month}"
        }
        
        return "${nepaliDate.dayOfMonth} ${monthName} ${nepaliDate.year}"
    }

    private fun convertUIDateToNepaliFormat(uiDate: String): String? {
        if (uiDate.isBlank()) return null
        
        // Convert from dd-mm-yyyy (UI format) to yyyy-mm-dd (API format)
        return try {
            val parts = uiDate.split("-")
            if (parts.size == 3) {
                val day = parts[0].padStart(2, '0')
                val month = parts[1].padStart(2, '0')
                val year = parts[2]
                "$year-$month-$day"
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}