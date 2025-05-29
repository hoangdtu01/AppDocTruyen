package com.example.apptruyencopy.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.apptruyencopy.R
import com.example.apptruyencopy.di.getViewModel
import com.example.apptruyencopy.model.Manga
import com.example.apptruyencopy.model.RetrofitClient
import com.example.apptruyencopy.repository.AuthRepository
import com.example.apptruyencopy.repository.MangaRepository
import com.example.apptruyencopy.viewmodel.AuthViewModel
import com.example.apptruyencopy.viewmodel.HomeViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel { 
        val repository = MangaRepository(RetrofitClient.mangaDexApi)
        HomeViewModel(repository) 
    },
    authViewModel: AuthViewModel = viewModel {
        AuthViewModel(AuthRepository())
    }
) {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    val scrollState = rememberScrollState()
    
    val filteredMangas by viewModel.filteredMangas
    val topRatedMangas by viewModel.topRatedMangas
    val recentlyUpdatedMangas by viewModel.recentlyUpdatedMangas
    val isLoading by viewModel.isLoading
    
    // Theo dõi tab hiện tại
    var selectedTab by remember { mutableStateOf(1) } // Mặc định là "Truyện" (index 1)

    // Kích hoạt lấy dữ liệu khi màn hình được tạo
    LaunchedEffect(Unit) {
        viewModel.loadTopRatedMangas()
        viewModel.loadRecentlyUpdatedMangas()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nguồn MangaDex") },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .height(44.dp)
                            .width(200.dp)
                            .background(
                                color = Color(0xFFE3ECF7), // xanh nhạt
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { navController.navigate("search") },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 10.dp)
                        ) {
                            Text(
                                text = "Tìm Kiếm Truyện",
                                color = Color(0xFF6B7683),
                                fontSize = 20.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )

                            Spacer(modifier = Modifier.width(5.dp))

                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Tìm kiếm",
                                tint = Color(0xFF6B7683),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                // Tủ sách / Yêu thích
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Bookmarks, contentDescription = "Yêu thích") },
                    label = { Text("Yêu thích") },
                    selected = selectedTab == 0,
                    onClick = { 
                        selectedTab = 0
                        navController.navigate("favorites") 
                    }
                )
                
                // Truyện (Home)
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Truyện") },
                    label = { Text("Truyện") },
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        // Đã ở trang home nên không cần navigate
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
                
                // Lịch sử đọc truyện
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, contentDescription = "Lịch sử") },
                    label = { Text("Lịch sử") },
                    selected = selectedTab == 3,
                    onClick = { 
                        selectedTab = 3
                        navController.navigate("history") 
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
            ) {
                // Phần Khuyến khích đọc
                SectionTitle("KHUYẾN KHÍCH ĐỌC", Icons.Default.Favorite)
                
                if (topRatedMangas.isNotEmpty()) {
                    MangaCarousel(
                        mangas = topRatedMangas,
                        onMangaClick = { mangaId -> navController.navigate("chapters/$mangaId") }
                    )
                }
                
                // Phần Truyện mới cập nhật
                SectionTitle("TRUYỆN MỚI CẬP NHẬT", Icons.Default.NewReleases)
                
                if (recentlyUpdatedMangas.isNotEmpty()) {
                    MangaCarousel(
                        mangas = recentlyUpdatedMangas,
                        onMangaClick = { mangaId -> navController.navigate("chapters/$mangaId") }
                    )
                }
                
                // Phần hiển thị tất cả truyện (giống như hiện tại)
                SectionTitle("TẤT CẢ TRUYỆN", Icons.Default.ListAlt)
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp) // Giới hạn chiều cao để có thể scroll
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
                }
                
                // Thêm khoảng trống ở cuối để không bị navigation bar che
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Icon(
            imageVector = Icons.Default.NavigateNext,
            contentDescription = "Xem thêm",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun MangaCarousel(
    mangas: List<Manga>,
    onMangaClick: (String) -> Unit
) {
    val pagerState = rememberPagerState()
    
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            count = mangas.size.coerceAtMost(5),
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) { page ->
            val manga = mangas[page]
            val title = manga.attributes.title.values.firstOrNull() ?: "No Title"
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onMangaClick(manga.id) },
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Ảnh bìa
                    if (manga.imageUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = manga.imageUrl,
                                error = painterResource(R.drawable.placeholder)
                            ),
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.BrokenImage,
                                contentDescription = "No cover",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                    
                    // Overlay màu tối và text ở dưới
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                    startY = 0f,
                                    endY = 600f
                                )
                            )
                    )
                    
                    // Tiêu đề truyện
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Chapter ${page + 1}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Chỉ số trang
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp),
            activeColor = MaterialTheme.colorScheme.primary,
            inactiveColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        // Hiển thị dưới dạng danh sách ngang
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mangas) { manga ->
                val title = manga.attributes.title.values.firstOrNull() ?: "No Title"
                
                Card(
                    modifier = Modifier
                        .width(120.dp)
                        .clickable { onMangaClick(manga.id) },
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column {
                        if (manga.imageUrl != null) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = manga.imageUrl,
                                    error = painterResource(R.drawable.placeholder)
                                ),
                                contentDescription = title,
                                modifier = Modifier
                                    .height(160.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .height(160.dp)
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.BrokenImage,
                                    contentDescription = "No cover",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Text(
                            text = title,
                            modifier = Modifier.padding(4.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
} 