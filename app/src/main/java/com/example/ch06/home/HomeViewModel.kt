package com.example.ch06.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ch06.data.Article
import com.example.ch06.data.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class HomeViewModel(
    private val repository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var allArticles: List<Article> = emptyList()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching { repository.getArticles() }
                .onSuccess { articles ->
                    // simpan semua article dan menerapkan filter
                    allArticles = articles
                    var currentQuery = _uiState.value.query
                    val filteredArticles = filter(allArticles, currentQuery)
                    _uiState.update { it.copy(isLoading = false, articles = articles) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Gagal memuat artikel"
                        )
                    }
                }
        }
    }

    // filtering berdasarkan query
    fun onQueryChange(newQuery: String) {
        _uiState.update { currentState ->
            val filteredArticles = filter(allArticles, newQuery)
            currentState.copy(
                query = newQuery,
                articles = filteredArticles
            )
        }
    }

    private fun filter(source: List<Article>, query: String): List<Article> {
        if (query.isBlank()) return source
        val trimmedQuery = query.trim()
        return source.filter { article ->
            article.title.contains(trimmedQuery, ignoreCase = true) ||
                    article.category.contains(trimmedQuery, ignoreCase = true)
        }
    }
}
