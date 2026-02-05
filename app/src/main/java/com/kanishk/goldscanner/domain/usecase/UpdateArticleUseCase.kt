package com.kanishk.goldscanner.domain.usecase

import com.goldscanner.data.common.Result
import com.kanishk.goldscanner.data.model.request.UpdateArticleRequest
import com.kanishk.goldscanner.data.model.response.GoldArticleResponse
import com.kanishk.goldscanner.domain.repository.GoldArticleRepository

class UpdateArticleUseCase(
    private val goldArticleRepository: GoldArticleRepository
) {
    suspend operator fun invoke(
        articleId: String,
        request: UpdateArticleRequest
    ): Result<GoldArticleResponse> {
        return goldArticleRepository.updateArticle(articleId, request)
    }
}