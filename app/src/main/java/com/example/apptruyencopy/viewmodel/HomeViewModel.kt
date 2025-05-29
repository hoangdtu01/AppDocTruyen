package com.example.apptruyencopy.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptruyencopy.model.Manga
import com.example.apptruyencopy.repository.MangaRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: MangaRepository) : ViewModel() {
    
    private val _mangas = mutableStateOf<List<Manga>>(emptyList())
    val mangas: State<List<Manga>> = _mangas
    
    private val _filteredMangas = mutableStateOf<List<Manga>>(emptyList())
    val filteredMangas: State<List<Manga>> = _filteredMangas
    
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    private val _chapterCounts = mutableStateOf<Map<String, String>>(emptyMap())
    val chapterCounts: State<Map<String, String>> = _chapterCounts
    
    // Thêm state cho truyện có rating cao
    private val _topRatedMangas = mutableStateOf<List<Manga>>(emptyList())
    val topRatedMangas: State<List<Manga>> = _topRatedMangas
    
    // Thêm state cho truyện mới cập nhật
    private val _recentlyUpdatedMangas = mutableStateOf<List<Manga>>(emptyList())
    val recentlyUpdatedMangas: State<List<Manga>> = _recentlyUpdatedMangas
    
    init {
        loadMangas()
    }
    
    fun loadMangas() {
        _isLoading.value = true
        viewModelScope.launch {
            val mangaList = repository.getMangaList()
            _mangas.value = mangaList
            _filteredMangas.value = mangaList
            
            // Load số chapter cho mỗi manga
            val counts = mutableMapOf<String, String>()
            mangaList.forEach { manga ->
                try {
                    val total = repository.getChapterCount(manga.id, "en")
                    counts[manga.id] = total.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            _chapterCounts.value = counts
            _isLoading.value = false
        }
    }
    
    fun loadTopRatedMangas() {
        viewModelScope.launch {
            try {
                val topRatedList = repository.getTopRatedMangas()
                _topRatedMangas.value = topRatedList
            } catch (e: Exception) {
                e.printStackTrace()
                _topRatedMangas.value = emptyList()
            }
        }
    }
    
    fun loadRecentlyUpdatedMangas() {
        viewModelScope.launch {
            try {
                val recentlyUpdatedList = repository.getRecentlyUpdatedMangas()
                _recentlyUpdatedMangas.value = recentlyUpdatedList
            } catch (e: Exception) {
                e.printStackTrace()
                _recentlyUpdatedMangas.value = emptyList()
            }
        }
    }
    
    fun filterBySearch(searchText: String) {
        _filteredMangas.value = _mangas.value.filter { manga ->
            manga.attributes.title.values.firstOrNull()?.contains(searchText, true) == true
        }
    }
    
    fun filterByGenre(genre: String) {
        _filteredMangas.value = if (genre == "Tất cả") {
            _mangas.value
        } else {
            _mangas.value.filter { 
                it.attributes.tags.any { tag -> 
                    tag.attributes.name["en"]?.contains(genre, true) == true 
                } 
            }
        }
    }
    
    fun resetFilters() {
        _filteredMangas.value = _mangas.value
    }
} 