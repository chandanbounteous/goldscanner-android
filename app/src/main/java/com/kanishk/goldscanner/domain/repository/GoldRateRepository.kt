package com.kanishk.goldscanner.domain.repository

import com.kanishk.goldscanner.data.model.response.GoldRateResponse
import com.goldscanner.data.common.Result

interface GoldRateRepository {
    suspend fun getCurrentGoldRate(): Result<GoldRateResponse>
}