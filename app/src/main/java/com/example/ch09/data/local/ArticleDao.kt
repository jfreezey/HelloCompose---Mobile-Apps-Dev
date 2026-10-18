package com.example.ch09.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class ArticleDao {
    @Query("""
        SELECT * FROM articles
        WHERE category = :category
        ORDER BY position ASC
    """)
    abstract suspend fun getArticles(
        category: String
    ): List<ArticleEntity>

    // Ambil satu artikel berdasarkan URL
    @Query("""
        SELECT * FROM articles
        WHERE url = :url
        LIMIT 1
    """)
    abstract suspend fun getArticleByUrl(
        url: String
    ): ArticleEntity?

    // Cari berita dari seluruh cache
    @Query("""
        SELECT * FROM articles
        WHERE title LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
        ORDER BY cachedAt DESC, position ASC
    """)
    abstract suspend fun searchCached(
        query: String
    ): List<ArticleEntity>

    // Simpan artikel
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(
        articles: List<ArticleEntity>
    )

    // Hapus cache
    @Query("""
        DELETE FROM articles
        WHERE category = :category
    """)
    abstract suspend fun deleteCategory(
        category: String
    )

    // Mengganti cache kategori dalam satu transaksi
    @Transaction
    open suspend fun replaceCategory(
        category: String,
        articles: List<ArticleEntity>
    ) {
        deleteCategory(category)
        insertAll(articles)
    }
}