package com.example.apptruyencopy.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.apptruyencopy.model.RetrofitClient
import com.example.apptruyencopy.repository.MangaRepository
import com.example.apptruyencopy.viewmodel.ChaptersViewModel

@Composable
fun ChaptersScreen(
    navController: NavController, 
    mangaId: String,
    viewModel: ChaptersViewModel = viewModel {
        val repository = MangaRepository(RetrofitClient.mangaDexApi)
        ChaptersViewModel(repository)
    }
) {
    val manga by viewModel.manga
    val chapters by viewModel.chapters
    val selectedLanguage by viewModel.selectedLanguage
    val isLoading by viewModel.isLoading
    val totalEnChapters by viewModel.totalEnChapters
    val languageOptions = viewModel.languageOptions
    
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
            }

            Column(modifier = Modifier.padding(16.dp)) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { navController.navigate("reader/${chapter.id}") },
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