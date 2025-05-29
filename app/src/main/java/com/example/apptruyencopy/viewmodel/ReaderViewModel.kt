package com.example.apptruyencopy.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptruyencopy.repository.MangaRepository
import kotlinx.coroutines.launch

class ReaderViewModel(private val repository: MangaRepository) : ViewModel() {
    
    private val _pageUrls = mutableStateOf<List<String>>(emptyList())
    val pageUrls: State<List<String>> = _pageUrls
    
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    fun loadChapterPages(chapterId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val urls = repository.getChapterPages(chapterId)
                _pageUrls.value = urls
            } catch (e: Exception) {
                e.printStackTrace()
                _pageUrls.value = emptyList()
            }
            _isLoading.value = false
        }
    }
} 