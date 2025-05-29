package com.example.apptruyencopy.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.apptruyencopy.R
import com.example.apptruyencopy.di.getViewModel
import com.example.apptruyencopy.model.Genre
import com.example.apptruyencopy.model.RetrofitClient
import com.example.apptruyencopy.repository.MangaRepository
import com.example.apptruyencopy.viewmodel.GenresViewModel
import com.example.apptruyencopy.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenresScreen(
    navController: NavController,
    viewModel: GenresViewModel = viewModel {
        val repository = MangaRepository(RetrofitClient.mangaDexApi)
        GenresViewModel(repository)
    }
) {
    val filteredGenres by viewModel.filteredGenres
    val searchQuery by viewModel.searchQuery
    val selectedGenreIds by viewModel.selectedGenreIds
    val filteredMangas by viewModel.filteredMangas
    val isLoading by viewModel.isLoading
    
    // Theo dõi tab hiện tại cho bottom navigation
    var selectedTab by remember { mutableStateOf(2) } // Tab "Thể loại" là số 2
    
    // Theo dõi trạng thái hiển thị (danh sách thể loại hoặc kết quả lọc)
    var showResults by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showResults) "Kết quả lọc" else "Lọc thể loại") },
                navigationIcon = {
                    if (showResults) {
                        IconButton(onClick = { showResults = false }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Quay lại"
                            )
                        }
                    }
                },
                actions = {
                    if (!showResults) {
                        IconButton(onClick = { viewModel.clearAllSelections() }) {
                            Icon(
                                Icons.Default.ClearAll,
                                contentDescription = "Xóa tất cả"
                            )
                        }
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
                        // navController.navigate("bookmarks")
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
                        // Đã ở màn hình thể loại
                    }
                )
                
                // Lịch sử đọc
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, contentDescription = "Lịch sử") },
                    label = { Text("Lịch sử") },
                    selected = selectedTab == 3,
                    onClick = { 
                        selectedTab = 3
                        // navController.navigate("history")
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
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (showResults) {
                // Hiển thị kết quả lọc theo thể loại
                Column(modifier = Modifier.padding(innerPadding)) {
                    // Hiển thị danh sách thể loại đã chọn như là tag
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredGenres.filter { it.isSelected }) { genre ->
                            val genreName = genre.attributes.name["en"] ?: "Unknown"
                            SuggestionChip(
                                onClick = { /* Do nothing */ },
                                label = { Text(genreName) }
                            )
                        }
                    }
                    
                    if (filteredMangas.isEmpty() && !isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Không tìm thấy truyện phù hợp")
                        }
                    } else {
                        val lazyGridState = rememberLazyGridState()
                        
                        // Theo dõi vị trí cuộn để tải thêm
                        LaunchedEffect(lazyGridState) {
                            snapshotFlow { 
                                lazyGridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            }.collect { lastVisibleItem ->
                                viewModel.checkIfShouldLoadMore(lastVisibleItem)
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                state = lazyGridState,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredMangas) { manga ->
                                    val title = manga.attributes.title.values.firstOrNull() ?: "No Title"
                                    
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { navController.navigate("chapters/${manga.id}") },
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            if (manga.imageUrl != null) {
                                                Image(
                                                    painter = rememberAsyncImagePainter(
                                                        model = manga.imageUrl,
                                                        error = painterResource(R.drawable.placeholder)
                                                    ),
                                                    contentDescription = title,
                                                    modifier = Modifier
                                                        .height(180.dp)
                                                        .fillMaxWidth(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .height(180.dp)
                                                        .fillMaxWidth()
                                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Filled.BrokenImage,
                                                        contentDescription = "No cover",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                                
                                // Hiển thị indicator khi đang tải thêm
                                if (viewModel.isLoadingMore.value) {
                                    item(span = { GridItemSpan(maxLineSpan) }) {
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
                                
                                // Hiển thị nút "Tải thêm" nếu còn dữ liệu
                                if (viewModel.hasMoreData.value && !viewModel.isLoadingMore.value &&
                                    filteredMangas.isNotEmpty()) {
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Button(onClick = { viewModel.loadNextPage() }) {
                                                Text("Tải thêm")
                                            }
                                        }
                                    }
                                }
                                
                                // Hiển thị thông báo khi đã tải hết
                                if (!viewModel.hasMoreData.value && filteredMangas.isNotEmpty()) {
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        Text(
                                            text = "Đã hiển thị tất cả truyện",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                            
                            // Loading overlay khi đang tải lần đầu
                            if (isLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            } else {
                // Hiển thị danh sách thể loại để lọc
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Thanh tìm kiếm thể loại
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        placeholder = { Text("Tìm kiếm thể loại") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )
                    
                    // Hiển thị số lượng thể loại đã chọn
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Đã chọn: ${selectedGenreIds.size} thể loại",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Button(
                            onClick = {
                                viewModel.applyGenresFilter()
                                showResults = true
                            },
                            enabled = selectedGenreIds.isNotEmpty()
                        ) {
                            Text("Lọc")
                        }
                    }
                    
                    // Danh sách thể loại với checkbox
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(filteredGenres) { genre ->
                            GenreItem(
                                genre = genre,
                                onToggle = { viewModel.toggleGenreSelection(genre.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GenreItem(genre: Genre, onToggle: () -> Unit) {
    val name = genre.attributes.name["en"] ?: "Unknown"
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Checkbox(
                checked = genre.isSelected,
                onCheckedChange = { onToggle() }
            )
        }
    }
} 