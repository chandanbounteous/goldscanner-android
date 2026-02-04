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
}