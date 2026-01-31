package com.kanishk.goldscanner.domain.usecase

import com.kanishk.goldscanner.domain.repository.GoldArticleRepository
import com.kanishk.goldscanner.data.model.GoldArticleWithCalculation
import com.kanishk.goldscanner.data.model.response.PaginationInfo
import com.goldscanner.data.common.Result
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.kotlin.*

/**
 * Test class for GetGoldArticlesUseCase
 * Tests the core business logic of retrieving gold articles with pagination
 */
class GetGoldArticlesUseCaseTest {

    private lateinit var mockRepository: GoldArticleRepository
    private lateinit var useCase: GetGoldArticlesUseCase
    
    @Before
    fun setup() {
        mockRepository = mock()
        useCase = GetGoldArticlesUseCase(mockRepository)
    }

    @Test
    fun `test invoke calls repository with correct parameters`() = runBlocking {
        // Mock response
        val articles = emptyList<GoldArticleWithCalculation>()
        val pagination = PaginationInfo(0, 25, 0, false)
        val expectedResult = Result.Success(Pair(articles, pagination))
        
        whenever(mockRepository.getGoldArticles("TEST", 0, 25))
            .thenReturn(expectedResult)
        
        // Test
        val result = useCase("TEST", 0, 25)
        
        // Verify
        assertEquals(expectedResult, result)
        verify(mockRepository).getGoldArticles("TEST", 0, 25)
    }

    @Test
    fun `test invoke with default parameters`() = runBlocking {
        // Mock response
        val articles = emptyList<GoldArticleWithCalculation>()
        val pagination = PaginationInfo(0, 25, 0, false)
        val expectedResult = Result.Success(Pair(articles, pagination))
        
        whenever(mockRepository.getGoldArticles("", 0, 25))
            .thenReturn(expectedResult)
        
        // Test with defaults
        val result = useCase()
        
        // Verify default parameters are used
        assertEquals(expectedResult, result)
        verify(mockRepository).getGoldArticles("", 0, 25)
    }
}