package com.kanishk.goldscanner.domain.repository

import com.kanishk.goldscanner.data.model.GoldArticleWithCalculation
import com.kanishk.goldscanner.data.model.response.PaginationInfo
import com.goldscanner.data.common.Result
import kotlinx.coroutines.flow.Flow

interface GoldArticleRepository {
    suspend fun getGoldArticles(
        code: String? = null,
        offset: Int = 0,
        limit: Int = 20
    ): Result<Pair<List<GoldArticleWithCalculation>, PaginationInfo>>
    
    fun getDefaultOffset(): Int
    fun getDefaultLimit(): Int
    fun setDefaultOffset(offset: Int)
    fun setDefaultLimit(limit: Int)
}