package com.example.ch09.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ch09.data.NewsRepository
import com.example.ch09.data.model.Article
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider


data class ArticleDetailUiState(
    val isLoading: Boolean = true,
    val article: Article? = null,
    val errorMessage: String? = null
)

class ArticleDetailViewModel(
    private val repository: NewsRepository,
    private val articleUrl: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleDetailUiState())
    val uiState: StateFlow<ArticleDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch{
            val article = repository.getArticle(articleUrl)

            if (article != null) {
                _uiState.value = ArticleDetailUiState(
                    isLoading = false,
                    article = article
                )
            } else {
                _uiState.value = ArticleDetailUiState(
                    isLoading = false,
                    errorMessage = "Artikel tidak ditemukan"
                )
            }
        }
    }
}

class ArticleDetailViewModelFactory(
    private val repository: NewsRepository,
    private val articleUrl: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        return ArticleDetailViewModel(
            repository,
            articleUrl
        ) as T
    }
}
