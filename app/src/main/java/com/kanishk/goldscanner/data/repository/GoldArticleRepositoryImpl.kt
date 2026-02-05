package com.kanishk.goldscanner.data.repository

import com.kanishk.goldscanner.data.network.service.GoldRateApiService
import com.kanishk.goldscanner.data.network.service.GoldArticleApiService
import com.kanishk.goldscanner.domain.repository.GoldArticleRepository
import com.kanishk.goldscanner.domain.repository.GoldRateRepository
import com.kanishk.goldscanner.data.model.GoldArticleWithCalculation
import com.kanishk.goldscanner.data.model.request.CreateArticleRequest
import com.kanishk.goldscanner.data.model.request.UpdateArticleRequest
import com.kanishk.goldscanner.data.model.response.GoldArticleResponse
import com.kanishk.goldscanner.data.model.response.PaginationInfo
import com.kanishk.goldscanner.data.model.response.GoldArticlesResponse
import com.kanishk.goldscanner.utils.LocalStorage
import com.kanishk.goldscanner.utils.GoldArticleCalculator
import com.goldscanner.data.common.Result
import com.goldscanner.data.common.ErrorResponse
import com.kanishk.goldscanner.data.network.ApiException
import com.kanishk.goldscanner.data.network.AuthenticationException

class GoldArticleRepositoryImpl(
    private val goldRateApiService: GoldRateApiService,
    private val goldArticleApiService: GoldArticleApiService,
    private val goldRateRepository: GoldRateRepository,
    private val localStorage: LocalStorage
) : GoldArticleRepository {

    override suspend fun getGoldArticles(
        code: String?,
        offset: Int,
        limit: Int
    ): Result<Pair<List<GoldArticleWithCalculation>, PaginationInfo>> {
        return try {
            // First get current gold rate for calculations
            val goldRateResult = goldRateRepository.getCurrentGoldRate()
            if (goldRateResult is Result.Error) {
                return Result.Error(goldRateResult.errorResponse)
            }
            
            val goldRateResponse = (goldRateResult as Result.Success).data
            val currentGoldRate24K = goldRateResponse.rates.getValue("24").toDouble()
            
            // Get articles from API
            val articlesResponse: GoldArticlesResponse = goldRateApiService.getGoldArticles(code, offset, limit)
            
            // Calculate costs for each article
            val articlesWithCalculation = articlesResponse.body.articles.map { article ->
                val calculation = GoldArticleCalculator.calculateArticleCost(
                    currentGoldRate24KPerTola = currentGoldRate24K,
                    netWeight = article.netWeight,
                    karat = article.karat,
                    addOnCost = article.addOnCost,
                    discount = 0.0 // Default discount
                )
                
                GoldArticleWithCalculation(article, calculation)
            }
            
            Result.Success(Pair(articlesWithCalculation, articlesResponse.body.pagination))
            
        } catch (e: AuthenticationException) {
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

    override suspend fun createArticle(request: CreateArticleRequest): Result<GoldArticleResponse> {
        return try {
            val response = goldArticleApiService.createArticle(request)
            Result.Success(response)
        } catch (e: AuthenticationException) {
            Result.Error(ErrorResponse.authenticationError(e.message ?: "Authentication failed"))
        } catch (e: ApiException.ClientError) {
            Result.Error(ErrorResponse.clientError(e.code, e.message, e.body))
        } catch (e: ApiException.NetworkError) {
            Result.Error(ErrorResponse.networkError(e.message))
        } catch (e: Exception) {
            Result.Error(ErrorResponse.networkError("An unexpected error occurred: ${e.message}"))
        }
    }

    override suspend fun updateArticle(articleId: String, request: UpdateArticleRequest): Result<GoldArticleResponse> {
        return try {
            val response = goldArticleApiService.updateArticle(articleId, request)
            Result.Success(response)
        } catch (e: AuthenticationException) {
            Result.Error(ErrorResponse.authenticationError(e.message ?: "Authentication failed"))
        } catch (e: ApiException.ClientError) {
            Result.Error(ErrorResponse.clientError(e.code, e.message, e.body))
        } catch (e: ApiException.NetworkError) {
            Result.Error(ErrorResponse.networkError(e.message))
        } catch (e: Exception) {
            Result.Error(ErrorResponse.networkError("An unexpected error occurred: ${e.message}"))
        }
    }

    override fun getDefaultOffset(): Int {
        return localStorage.getValue<Int>(LocalStorage.StorageKey.DEFAULT_ARTICLES_OFFSET) ?: 0
    }

    override fun getDefaultLimit(): Int {
        return localStorage.getValue<Int>(LocalStorage.StorageKey.DEFAULT_ARTICLES_LIMIT) ?: 20
    }

    override fun setDefaultOffset(offset: Int) {
        localStorage.save(LocalStorage.StorageKey.DEFAULT_ARTICLES_OFFSET, offset)
    }

    override fun setDefaultLimit(limit: Int) {
        localStorage.save(LocalStorage.StorageKey.DEFAULT_ARTICLES_LIMIT, limit)
    }
}