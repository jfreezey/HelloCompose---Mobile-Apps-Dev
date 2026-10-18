package com.example.ch09.ui.detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ch09.KabarKiniApp
import com.example.ch09.MainApplication
import com.example.ch09.ui.openUrl

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ArticleDetailRoute(
    articleUrl: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val repository =
        (context.applicationContext as MainApplication)
            .container.newsRepository

    val viewModel: ArticleDetailViewModel = viewModel(
        key = articleUrl,
        factory = ArticleDetailViewModelFactory(
            repository,
            articleUrl
        )
    )

    val uiState by viewModel.uiState.collectAsState()

    ArticleDetailScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onReadMore = { url ->
            openUrl(context, url)
        }
    )
}