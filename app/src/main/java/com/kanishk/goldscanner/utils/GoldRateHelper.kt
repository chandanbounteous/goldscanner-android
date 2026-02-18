package com.kanishk.goldscanner.utils

import com.kanishk.goldscanner.data.model.response.GoldRateResponse

object GoldRateHelper {
    
    /**
     * Extract gold rate for specific karat from GoldRateResponse
     */
    fun getGoldRateForKarat(goldRateResponse: GoldRateResponse?, karat: Int): Double {
        if (goldRateResponse == null) return 0.0
        
        return when (karat) {
            24 -> goldRateResponse.rates["24"] ?: 0.0
            22 -> goldRateResponse.rates["22"] ?: 0.0
            18 -> goldRateResponse.rates["18"] ?: 0.0
            14 -> goldRateResponse.rates["14"] ?: 0.0
            else -> 0.0
        }
    }
    
    /**
     * Get 24 karat gold rate from GoldRateResponse
     */
    fun get24KaratGoldRate(goldRateResponse: GoldRateResponse?): Double {
        return goldRateResponse?.rates?.get("24") ?: 0.0
    }
    
    /**
     * Determine effective gold rate for basket item calculations
     * @param basket BasketDetail containing rate information
     * @param currentGoldRate Current gold rate from API
     * @return Effective gold rate to use for calculations
     */
    fun getEffectiveGoldRateForBasket(
        basket: com.kanishk.goldscanner.data.model.response.BasketDetail, 
        currentGoldRate: Double
    ): Double {
        // Check if basket was created today and has a fixed rate
        val basketCreatedDate = java.time.OffsetDateTime.parse(basket.createdAt).toLocalDate()
        val today = java.time.LocalDate.now()
        
        return if (basket.isGoldRateFixed && basketCreatedDate == today && basket.fixedGoldRate24KPerTola != null) {
            // Use fixed rate if basket was created today with fixed rate
            basket.fixedGoldRate24KPerTola
        } else {
            // Use current market rate
            currentGoldRate
        }
    }
}