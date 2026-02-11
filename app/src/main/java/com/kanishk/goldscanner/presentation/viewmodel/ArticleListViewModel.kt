package com.kanishk.goldscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanishk.goldscanner.domain.usecase.GetGoldArticlesUseCase
import com.kanishk.goldscanner.data.model.GoldArticleWithCalculation
import com.kanishk.goldscanner.data.model.response.PaginationInfo
import com.kanishk.goldscanner.data.model.response.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

data class ArticleListUiState(
    val articles: List<GoldArticleWithCalculation> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val currentOffset: Int = 0,
    val currentLimit: Int = 25
)

class ArticleListViewModel(
    private val goldArticleUseCase: GetGoldArticlesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleListUiState())
    val uiState: StateFlow<ArticleListUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private val searchDebounceTime = 500L // 500ms debounce

    init {
        loadArticles()
    }

    fun loadArticles(isLoadMore: Boolean = false) {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            if (isLoadMore && (!currentState.hasMore || currentState.isLoadingMore)) {
                return@launch
            }

            _uiState.value = currentState.copy(
                isLoading = !isLoadMore,
                isLoadingMore = isLoadMore,
                error = null
            )

            val offset = if (isLoadMore) currentState.currentOffset + 1 else 0
            val result = goldArticleUseCase(
                code = currentState.searchQuery,
                offset = offset,
                limit = currentState.currentLimit
            )

            when (result) {
                is Result.Success -> {
                    val (newArticles, paginationInfo) = result.data
                    val updatedArticles = if (isLoadMore) {
                        currentState.articles + newArticles
                    } else {
                        newArticles
                    }

                    _uiState.value = currentState.copy(
                        articles = updatedArticles,
                        isLoading = false,
                        isLoadingMore = false,
                        hasMore = paginationInfo.hasMore,
                        currentOffset = paginationInfo.offset,
                        error = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = result.errorResponse.message ?: "Failed to load articles"
                    )
                }
                else -> {
                    // Handle any other result types
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        // Cancel previous search job
        searchJob?.cancel()
        
        // Start new search job with debounce
        searchJob = viewModelScope.launch {
            delay(searchDebounceTime)
            loadArticles(isLoadMore = false)
        }
    }

    fun onLoadMore() {
        loadArticles(isLoadMore = true)
    }

    fun onRefresh() {
        _uiState.value = _uiState.value.copy(currentOffset = 0)
        loadArticles(isLoadMore = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}