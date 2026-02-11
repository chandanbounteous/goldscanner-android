package com.kanishk.goldscanner.domain.usecase

import com.kanishk.goldscanner.domain.repository.GoldArticleRepository
import com.kanishk.goldscanner.data.model.request.CreateArticleRequest
import com.kanishk.goldscanner.data.model.response.GoldArticleResponse
import com.kanishk.goldscanner.data.model.response.Result

class CreateArticleUseCase(
    private val goldArticleRepository: GoldArticleRepository
) {
    suspend operator fun invoke(request: CreateArticleRequest): Result<GoldArticleResponse> {
        return goldArticleRepository.createArticle(request)
    }
}