package com.example.apptruyencopy.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.apptruyencopy.di.AppViewModelProvider
import com.example.apptruyencopy.model.Chapter
import com.example.apptruyencopy.viewmodel.ReaderViewModel
import com.google.firebase.auth.FirebaseAuth
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
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
    val chapters by viewModel.chapters
    val currentChapterIndex by viewModel.currentChapterIndex
    
    // State for chapter selection dialog
    var showChapterDialog by remember { mutableStateOf(false) }
    
    // Check authentication state
    val auth = FirebaseAuth.getInstance()
    val isUserLoggedIn = remember { auth.currentUser != null }
    
    // State for login dialog
    var showLoginPrompt by remember { mutableStateOf(false) }
    
    LaunchedEffect(chapterId) {
        viewModel.loadChapterPages(chapterId)
        
        // Load all chapters for navigation
        viewModel.loadMangaChapters(mangaId)
    }
    
    // Save reading history after chapters are loaded
    LaunchedEffect(chapters) {
        if (chapters.isNotEmpty()) {
            // Save reading history when the chapter is loaded (if user is logged in)
            viewModel.saveReadingHistory(
                mangaId = mangaId,
                chapterId = chapterId,
                title = title,
                coverUrl = coverUrl
            )
        }
    }
    
    // Show login prompt dialog if saving failed due to no authentication
    LaunchedEffect(saveHistoryStatus) {
        if (saveHistoryStatus == false && !isUserLoggedIn) {
            showLoginPrompt = true
        }
    }
    
    // Get chapter nav state for UI
    val hasPrevChapter = remember(chapters, currentChapterIndex) { 
        viewModel.hasPreviousChapter() 
    }
    val hasNextChapter = remember(chapters, currentChapterIndex) { 
        viewModel.hasNextChapter() 
    }
    val currentChapterNumber = remember(chapters, currentChapterIndex) { 
        viewModel.getCurrentChapterNumber() 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        // Navigate directly to the chapters screen instead of popping back stack
                        navController?.navigate("chapters/$mangaId") {
                            // Clear the back stack up to the chapters screen
                            popUpTo("chapters/$mangaId") {
                                inclusive = false
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                IconButton(
                    onClick = {
                        val prevChapterId = viewModel.getPreviousChapterId()
                        if (prevChapterId != null) {
                            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
                            val encodedCoverUrl = URLEncoder.encode(coverUrl, StandardCharsets.UTF_8.toString())
                            navController?.navigate("reader/${prevChapterId}/${mangaId}/${encodedTitle}/${encodedCoverUrl}")
                        }
                    },
                    enabled = hasPrevChapter
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIos,
                        contentDescription = "Previous Chapter",
                        tint = if (hasPrevChapter) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Current chapter button
                TextButton(
                    onClick = { 
                        if (chapters.isNotEmpty()) {
                            showChapterDialog = true 
                        }
                    }
                ) {
                    Text(
                        text = "Chapter $currentChapterNumber",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(
                    onClick = {
                        val nextChapterId = viewModel.getNextChapterId()
                        if (nextChapterId != null) {
                            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
                            val encodedCoverUrl = URLEncoder.encode(coverUrl, StandardCharsets.UTF_8.toString())
                            navController?.navigate("reader/${nextChapterId}/${mangaId}/${encodedTitle}/${encodedCoverUrl}")
                        }
                    },
                    enabled = hasNextChapter
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Next Chapter",
                        tint = if (hasNextChapter) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                    )
                }
            }
        }
    ) { paddingValues ->
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
        
        // Chapter selection dialog
        if (showChapterDialog) {
            AlertDialog(
                onDismissRequest = { showChapterDialog = false },
                title = { Text("Chọn Chapter") },
                text = {
                    if (chapters.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            items(chapters) { chapter ->
                                TextButton(
                                    onClick = {
                                        showChapterDialog = false
                                        val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
                                        val encodedCoverUrl = URLEncoder.encode(coverUrl, StandardCharsets.UTF_8.toString())
                                        navController?.navigate("reader/${chapter.id}/${mangaId}/${encodedTitle}/${encodedCoverUrl}")
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Chapter ${chapter.attributes.chapter ?: "?"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (chapter.id == chapterId) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showChapterDialog = false }) {
                        Text("Đóng")
                    }
                }
            )
        }

        if (isLoading || pageUrls.isEmpty()) {
            // Loading Indicator
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Hiển thị các trang ảnh
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp),
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
} 