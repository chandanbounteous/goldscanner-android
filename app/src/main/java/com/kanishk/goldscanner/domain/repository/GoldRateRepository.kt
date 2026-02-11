package com.kanishk.goldscanner.domain.repository

import com.kanishk.goldscanner.data.model.response.GoldRateResponse
import com.kanishk.goldscanner.data.model.response.Result

interface GoldRateRepository {
    suspend fun getCurrentGoldRate(): Result<GoldRateResponse>
}