package com.example.apptruyencopy.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.apptruyencopy.di.AppViewModelProvider
import com.example.apptruyencopy.viewmodel.ChaptersViewModel
import com.google.firebase.auth.FirebaseAuth
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun ChaptersScreen(
    navController: NavController, 
    mangaId: String,
    viewModel: ChaptersViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val manga by viewModel.manga
    val chapters by viewModel.chapters
    val selectedLanguage by viewModel.selectedLanguage
    val isLoading by viewModel.isLoading
    val totalEnChapters by viewModel.totalEnChapters
    val isFavorite by viewModel.isFavorite
    val languageOptions = viewModel.languageOptions
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
    
    LaunchedEffect(mangaId) {
        viewModel.loadMangaDetail(mangaId)
        viewModel.loadChapters(mangaId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Manga Info
        manga?.let {
            val title = it.attributes.title["en"] ?: it.attributes.title.values.firstOrNull() ?: "No title"
            val authors = it.relationships.filter { rel -> rel.type == "author" }
                .joinToString { rel -> rel.attributes?.name ?: "Unknown" }

            val tags = it.attributes.tags.mapNotNull { tag -> tag.attributes.name["en"] }

            val coverRel = it.relationships.find { rel -> rel.type == "cover_art" }
            val coverFileName = coverRel?.attributes?.fileName

            val coverUrl = coverFileName?.let { fileName ->
                "https://uploads.mangadex.org/covers/${it.id}/$fileName"
            } ?: ""

            Column(modifier = Modifier.padding(16.dp)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    
                    // Favorite Button
                    if (isLoggedIn) {
                        IconButton(
                            onClick = { viewModel.toggleFavorite(mangaId) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Text("Tựa: $title", style = MaterialTheme.typography.titleMedium)
                Text("Tác giả: $authors", style = MaterialTheme.typography.bodyMedium)
                Text("Thể loại: ${tags.joinToString()}", style = MaterialTheme.typography.bodySmall)
                Text("Tổng số chương (EN): $totalEnChapters", style = MaterialTheme.typography.bodySmall)
            }
        }

        // Language selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            languageOptions.forEach { (code, label) ->
                Button(
                    onClick = { viewModel.changeLanguage(code, mangaId) },
                    enabled = selectedLanguage != code
                ) {
                    Text(text = label)
                }
            }
        }

        // Chapter list
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (chapters.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không có chapter ${languageOptions[selectedLanguage]?.lowercase()}")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(chapters) { chapter ->
                    // Gather data needed for reading history
                    val currentManga = manga
                    if (currentManga != null) {
                        val title = currentManga.attributes.title["en"] 
                            ?: currentManga.attributes.title.values.firstOrNull() 
                            ?: "No title"
                        
                        val coverRel = currentManga.relationships.find { rel -> rel.type == "cover_art" }
                        val coverFileName = coverRel?.attributes?.fileName
                        val coverUrl = coverFileName?.let { fileName ->
                            "https://uploads.mangadex.org/covers/${currentManga.id}/$fileName"
                        } ?: ""
                        
                        // Encode title and coverUrl for navigation
                        val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
                        val encodedCoverUrl = URLEncoder.encode(coverUrl, StandardCharsets.UTF_8.toString())
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { 
                                    navController.navigate(
                                        "reader/${chapter.id}/${mangaId}/${encodedTitle}/${encodedCoverUrl}"
                                    ) 
                                },
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Chapter ${chapter.attributes.chapter ?: "?"}")
                            }
                        }
                    }
                }
            }
        }
    }
} 