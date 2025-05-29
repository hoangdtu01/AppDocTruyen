package com.example.apptruyencopy.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptruyencopy.model.Manga
import com.example.apptruyencopy.repository.FirebaseRepository
import com.example.apptruyencopy.repository.MangaRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val repository: MangaRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {
    
    private val _pageUrls = mutableStateOf<List<String>>(emptyList())
    val pageUrls: State<List<String>> = _pageUrls
    
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _saveHistoryStatus = mutableStateOf<Boolean?>(null)
    val saveHistoryStatus: State<Boolean?> = _saveHistoryStatus
    
    // Current authentication state
    private val auth = FirebaseAuth.getInstance()
    
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

    // Improved function to save reading history with better error handling
    fun saveReadingHistory(mangaId: String, chapterId: String, title: String, coverUrl: String) {
        // Only save history if user is logged in
        if (auth.currentUser == null) {
            _saveHistoryStatus.value = false
            return
        }
        
        _saveHistoryStatus.value = null
        viewModelScope.launch {
            try {
                val success = firebaseRepository.addToReadingHistory(
                    com.example.apptruyencopy.model.ReadingHistory(
                        mangaId = mangaId,
                        chapterId = chapterId,
                        title = title,
                        coverUrl = coverUrl
                    )
                )
                _saveHistoryStatus.value = success
            } catch (e: Exception) {
                e.printStackTrace()
                _saveHistoryStatus.value = false
            }
        }
    }

    // Reset status when needed
    fun resetSaveHistoryStatus() {
        _saveHistoryStatus.value = null
    }
} 