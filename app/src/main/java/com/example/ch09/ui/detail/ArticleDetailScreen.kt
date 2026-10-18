package com.example.ch09.ui.detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.ch09.data.model.Article
import com.example.ch09.util.formatRelativeTime
import java.time.Instant

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    uiState: ArticleDetailUiState,
    onNavigateBack: () -> Unit,
    onReadMore: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Berita") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.article != null -> {
                    ArticleDetailContent(
                        article = uiState.article,
                        onReadMore = onReadMore
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ArticleDetailContent(
    article: Article,
    onReadMore: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Gambar artikel
        if (!article.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = article.imageUrl,
                contentDescription = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Sumber dan waktu berita
        Text(
            text = "${article.sourceName} · ${
                formatRelativeTime(
                    now = System.currentTimeMillis(),
                    then = parsePublishedAt(article.publishedAt)
                )
            }",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Judul
        Text(
            text = article.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        // Author
        if (!article.author.isNullOrBlank()) {
            Text(
                text = "Oleh ${article.author}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider()

        // Deskripsi
        if (!article.description.isNullOrBlank()) {
            Text(
                text = article.description,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Potongan isi artikel
        if (!article.content.isNullOrBlank()) {
            Text(
                text = article.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onReadMore(article.url) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Baca Selengkapnya")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun parsePublishedAt(publishedAt: String): Long {
    return runCatching {
        Instant.parse(publishedAt).toEpochMilli()
    }.getOrDefault(0L)
}
