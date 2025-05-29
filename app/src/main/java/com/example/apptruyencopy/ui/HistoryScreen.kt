package com.example.apptruyencopy.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.apptruyencopy.di.AppViewModelProvider
import com.example.apptruyencopy.model.ReadingHistory
import com.example.apptruyencopy.viewmodel.UserViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: UserViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var selectedTab by remember { mutableStateOf(3) } // Tab "Lịch sử" là số 3
    val readingHistory by viewModel.readingHistory
    val isLoading by viewModel.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử đọc truyện") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        // Show dialog to confirm clearing history
                        // viewModel.clearReadingHistory()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa lịch sử")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                // Tủ sách
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Bookmarks, contentDescription = "Tủ sách") },
                    label = { Text("Tủ sách") },
                    selected = selectedTab == 0,
                    onClick = { 
                        selectedTab = 0
                        navController.navigate("bookmarks")
                    }
                )
                
                // Truyện (Home)
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Truyện") },
                    label = { Text("Truyện") },
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        navController.navigate("home")
                    }
                )
                
                // Thể loại
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Category, contentDescription = "Thể loại") },
                    label = { Text("Thể loại") },
                    selected = selectedTab == 2,
                    onClick = { 
                        selectedTab = 2
                        navController.navigate("genres")
                    }
                )
                
                // Lịch sử đọc
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, contentDescription = "Lịch sử") },
                    label = { Text("Lịch sử") },
                    selected = selectedTab == 3,
                    onClick = { 
                        selectedTab = 3
                        // Đã ở màn hình lịch sử
                    }
                )
                
                // Tôi (Profile)
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Tôi") },
                    label = { Text("Tôi") },
                    selected = selectedTab == 4,
                    onClick = { 
                        selectedTab = 4
                        navController.navigate("profile")
                    }
                )
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (readingHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chưa có lịch sử đọc truyện",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(readingHistory) { history ->
                    HistoryItem(
                        history = history,
                        onItemClick = { navController.navigate("chapters/${history.mangaId}") },
                        onReadClick = {
                            // Navigate directly to continue reading the chapter
                            val encodedTitle = URLEncoder.encode(history.title, StandardCharsets.UTF_8.toString())
                            val encodedCoverUrl = URLEncoder.encode(history.coverUrl, StandardCharsets.UTF_8.toString())
                            navController.navigate("reader/${history.chapterId}/${history.mangaId}/${encodedTitle}/${encodedCoverUrl}")
                        }
                    )
                }

                // Loading more indicator
                item {
                    if (viewModel.isLoadingMore.value) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Load more when reaching the end
                item {
                    if (viewModel.hasMoreHistory.value && !viewModel.isLoadingMore.value) {
                        LaunchedEffect(Unit) {
                            viewModel.loadMoreHistory()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(
    history: ReadingHistory,
    onItemClick: () -> Unit,
    onReadClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ảnh bìa truyện
            Image(
                painter = rememberAsyncImagePainter(history.coverUrl),
                contentDescription = history.title,
                modifier = Modifier
                    .width(80.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Thông tin truyện
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = history.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Chapter ${history.chapterId}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatTimestamp(history.timestamp.toDate()),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Button to continue reading
            IconButton(onClick = onReadClick) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Tiếp tục đọc",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun formatTimestamp(date: Date): String {
    val now = Calendar.getInstance()
    val timestampCal = Calendar.getInstance().apply { time = date }
    
    return when {
        // Cùng ngày
        now.get(Calendar.DATE) == timestampCal.get(Calendar.DATE) -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }
        // Cùng năm
        now.get(Calendar.YEAR) == timestampCal.get(Calendar.YEAR) -> {
            SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(date)
        }
        // Khác năm
        else -> {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
        }
    }
} 