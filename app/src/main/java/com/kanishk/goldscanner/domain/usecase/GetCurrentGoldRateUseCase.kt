package com.kanishk.goldscanner.domain.usecase

import com.kanishk.goldscanner.domain.repository.GoldRateRepository
import com.kanishk.goldscanner.data.model.response.GoldRateResponse
import com.kanishk.goldscanner.data.model.response.Result

class GetCurrentGoldRateUseCase(
    private val goldRateRepository: GoldRateRepository
) {
    suspend operator fun invoke(): Result<GoldRateResponse> {
        return goldRateRepository.getCurrentGoldRate()
    }
}