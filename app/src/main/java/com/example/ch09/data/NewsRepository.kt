package com.example.ch09.data

import com.example.ch09.data.local.ArticleDao
import com.example.ch09.data.mapper.toDomain
import com.example.ch09.data.mapper.toEntity
import com.example.ch09.data.model.Article
import com.example.ch09.data.model.NewsCategory
import com.example.ch09.data.model.NewsFeed
import com.example.ch09.data.model.OfflineInfo
import com.example.ch09.data.remote.NewsApiService
import com.example.ch09.data.remote.dto.NewsResponseDto
import kotlin.coroutines.cancellation.CancellationException

interface NewsRepository {
    suspend fun getTopHeadlines(category: NewsCategory): Result<NewsFeed>
    suspend fun searchNews(query: String): Result<NewsFeed>
    // Untuk layar detail (Tugas 2): artikel apa pun yang pernah dimuat
    suspend fun getArticle(url: String): Article?
}

class NewsRepositoryImpl(
    private val api: NewsApiService,
    private val dao: ArticleDao,
    private val clock: () -> Long = System::currentTimeMillis
) : NewsRepository {

    companion object {
        private const val SEARCH_KEY = "__search__"
    }

    override suspend fun getTopHeadlines(
        category: NewsCategory
    ): Result<NewsFeed> {
        return fetch(
            cacheKey = category.apiValue,
            remote = {
                api.getTopHeadlines(
                    category = category.apiValue
                )
            },
            readCache = {
                dao.getArticles(category.apiValue)
            }
        )
    }

    override suspend fun searchNews(
        query: String
    ): Result<NewsFeed> {
        return fetch(
            cacheKey = SEARCH_KEY,
            remote = {
                api.searchNews(query = query)
            },
            readCache = {
                dao.searchCached(query)
                    .distinctBy { it.url }
            }
        )
    }

    override suspend fun getArticle(
        url: String
    ): Article? {
        return dao.getArticleByUrl(url)?.toDomain()
    }

    private suspend fun fetch(
        cacheKey: String,
        remote: suspend () -> NewsResponseDto,
        readCache: suspend () ->
        List<com.example.ch09.data.local.ArticleEntity>
    ): Result<NewsFeed> {

        val result = runCatching {
            val articles = remote().articles
                .mapNotNull { it.toDomain() }
                .distinctBy { it.url }

            // Simpan artikel ke Room jika berhasil
            val cachedAt = clock()

            val entities = articles.mapIndexed { index, article ->
                article.toEntity(
                    category = cacheKey,
                    position = index,
                    cachedAt = cachedAt
                )
            }

            dao.replaceCategory(cacheKey, entities)

            NewsFeed(articles)
        }

        // runCatching juga menangkap CancellationException (mis. pengguna sudah
        // pindah kategori). Itu BUKAN error jaringan: lempar ulang.
        result.exceptionOrNull()?.let { if (it is CancellationException) throw it }

        if (result.isSuccess) {
            return result
        }

        val error = result.exceptionOrNull()!!

        val cachedResult = runCatching {
            readCache()
        }

        cachedResult.exceptionOrNull()?.let { cacheError ->
            if (cacheError is CancellationException) {
                throw cacheError
            }
        }

        val cachedArticles = cachedResult.getOrElse {
            return Result.failure(error)
        }

        // Cache kosong: tampilkan error asli
        if (cachedArticles.isEmpty()) {
            return Result.failure(error)
        }

        val articles = cachedArticles
            .distinctBy { it.url }
            .map { it.toDomain() }

        val cachedAt = cachedArticles
            .maxOf { it.cachedAt }

        // Cache tersedia: tampilkan data offline
        return Result.success(
            NewsFeed(
                articles = articles,
                offline = OfflineInfo(
                    cachedAt = cachedAt,
                    reason = error.toUserMessage()
                )
            )
        )
    }
}
