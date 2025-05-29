package com.example.apptruyencopy.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.apptruyencopy.R
import com.example.apptruyencopy.di.AppViewModelProvider
import com.example.apptruyencopy.model.Manga
import com.example.apptruyencopy.viewmodel.FavoritesViewModel

@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: FavoritesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val favoriteMangas by viewModel.favoriteMangas
    val isLoading by viewModel.isLoading
    val isLoggedIn by viewModel.isLoggedIn
    
    LaunchedEffect(Unit) {
        viewModel.loadFavoriteMangas()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "Truyện yêu thích",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (!isLoggedIn) {
            // User not logged in
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Bạn cần đăng nhập để xem truyện yêu thích",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.navigate("login") }) {
                        Text("Đăng nhập")
                    }
                }
            }
        } else if (isLoading) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (favoriteMangas.isEmpty()) {
            // No favorites
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bạn chưa có truyện yêu thích nào",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            // Display favorites in a grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favoriteMangas) { manga ->
                    FavoriteMangaItem(
                        manga = manga,
                        onMangaClick = { navController.navigate("chapters/${manga.id}") }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteMangaItem(manga: Manga, onMangaClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onMangaClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // Cover image
            manga.imageUrl?.let { imageUrl ->
                Image(
                    painter = rememberAsyncImagePainter(
                        model = imageUrl,
                        error = painterResource(R.drawable.placeholder)
                    ),
                    contentDescription = manga.attributes.title.values.firstOrNull(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Title
            Text(
                text = manga.attributes.title.values.firstOrNull() ?: "Không có tiêu đề",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
} 