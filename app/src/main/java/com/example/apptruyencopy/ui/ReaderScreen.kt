package com.example.apptruyencopy.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.apptruyencopy.model.RetrofitClient
import com.example.apptruyencopy.repository.MangaRepository
import com.example.apptruyencopy.viewmodel.ReaderViewModel

@Composable
fun ReaderScreen(
    chapterId: String,
    viewModel: ReaderViewModel = viewModel {
        val repository = MangaRepository(RetrofitClient.mangaDexApi)
        ReaderViewModel(repository)
    }
) {
    val pageUrls by viewModel.pageUrls
    val isLoading by viewModel.isLoading
    
    LaunchedEffect(chapterId) {
        viewModel.loadChapterPages(chapterId)
    }

    if (isLoading || pageUrls.isEmpty()) {
        // Loading Indicator
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        // Hiển thị các trang ảnh
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pageUrls) { pageUrl ->
                Image(
                    painter = rememberAsyncImagePainter(model = pageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.7f), // chỉnh tùy tỉ lệ hình
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    }
} 