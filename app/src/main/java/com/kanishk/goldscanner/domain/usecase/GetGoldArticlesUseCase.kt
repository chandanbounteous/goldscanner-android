package com.kanishk.goldscanner.domain.usecase

import com.kanishk.goldscanner.domain.repository.GoldArticleRepository
import com.kanishk.goldscanner.data.model.GoldArticleWithCalculation
import com.kanishk.goldscanner.data.model.response.PaginationInfo
import com.goldscanner.data.common.Result

class GetGoldArticlesUseCase(
    private val goldArticleRepository: GoldArticleRepository
) {
    suspend operator fun invoke(
        code: String = "",
        offset: Int = 0,
        limit: Int = 25
    ): Result<Pair<List<GoldArticleWithCalculation>, PaginationInfo>> {
        return goldArticleRepository.getGoldArticles(code, offset, limit)
    }
}