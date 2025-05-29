package com.example.apptruyencopy.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptruyencopy.model.Chapter
import com.example.apptruyencopy.model.Manga
import com.example.apptruyencopy.repository.MangaRepository
import kotlinx.coroutines.launch

class ChaptersViewModel(private val repository: MangaRepository) : ViewModel() {
    
    private val _manga = mutableStateOf<Manga?>(null)
    val manga: State<Manga?> = _manga
    
    private val _chapters = mutableStateOf<List<Chapter>>(emptyList())
    val chapters: State<List<Chapter>> = _chapters
    
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    private val _selectedLanguage = mutableStateOf("vi")
    val selectedLanguage: State<String> = _selectedLanguage
    
    private val _totalEnChapters = mutableStateOf(0)
    val totalEnChapters: State<Int> = _totalEnChapters
    
    val languageOptions = mapOf("vi" to "Tiếng Việt", "en" to "Tiếng Anh")
    
    fun loadMangaDetail(mangaId: String) {
        viewModelScope.launch {
            try {
                val mangaDetail = repository.getMangaDetail(mangaId)
                _manga.value = mangaDetail
                
                // Lấy tổng số chapter tiếng Anh
                val total = repository.getChapterCount(mangaId, "en")
                _totalEnChapters.value = total
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun loadChapters(mangaId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val chapterList = repository.getChapterList(mangaId, _selectedLanguage.value)
                _chapters.value = chapterList.sortedBy { it.attributes.chapter?.toFloatOrNull() ?: 0f }
            } catch (e: Exception) {
                _chapters.value = emptyList()
            }
            _isLoading.value = false
        }
    }
    
    fun changeLanguage(language: String, mangaId: String) {
        _selectedLanguage.value = language
        loadChapters(mangaId)
    }
} 