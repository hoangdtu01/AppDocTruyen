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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.apptruyencopy.di.AppViewModelProvider
import com.example.apptruyencopy.viewmodel.ReaderViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ReaderScreen(
    chapterId: String,
    mangaId: String,
    title: String,
    coverUrl: String,
    navController: NavController? = null,
    viewModel: ReaderViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val pageUrls by viewModel.pageUrls
    val isLoading by viewModel.isLoading
    val saveHistoryStatus by viewModel.saveHistoryStatus
    
    // Check authentication state
    val auth = FirebaseAuth.getInstance()
    val isUserLoggedIn = remember { auth.currentUser != null }
    
    // State for login dialog
    var showLoginPrompt by remember { mutableStateOf(false) }
    
    LaunchedEffect(chapterId) {
        viewModel.loadChapterPages(chapterId)
        
        // Save reading history when the chapter is loaded (if user is logged in)
        viewModel.saveReadingHistory(
            mangaId = mangaId,
            chapterId = chapterId,
            title = title,
            coverUrl = coverUrl
        )
    }
    
    // Show login prompt dialog if saving failed due to no authentication
    LaunchedEffect(saveHistoryStatus) {
        if (saveHistoryStatus == false && !isUserLoggedIn) {
            showLoginPrompt = true
        }
    }

    if (showLoginPrompt) {
        AlertDialog(
            onDismissRequest = { showLoginPrompt = false },
            title = { Text("Đăng nhập để lưu lịch sử") },
            text = { Text("Bạn cần đăng nhập để lưu lịch sử đọc truyện. Bạn có muốn đăng nhập ngay bây giờ không?") },
            confirmButton = {
                TextButton(onClick = {
                    navController?.navigate("login")
                    showLoginPrompt = false
                }) {
                    Text("Đăng nhập")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoginPrompt = false }) {
                    Text("Bỏ qua")
                }
            }
        )
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