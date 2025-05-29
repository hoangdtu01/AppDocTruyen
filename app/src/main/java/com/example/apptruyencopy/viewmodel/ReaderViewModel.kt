package com.example.apptruyencopy.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptruyencopy.model.Chapter
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
    
    private val _chapters = mutableStateOf<List<Chapter>>(emptyList())
    val chapters: State<List<Chapter>> = _chapters
    
    private val _currentChapterIndex = mutableStateOf<Int>(-1)
    val currentChapterIndex: State<Int> = _currentChapterIndex
    
    private val _currentChapterId = mutableStateOf("")
    val currentChapterId: State<String> = _currentChapterId
    
    // Current authentication state
    private val auth = FirebaseAuth.getInstance()
    
    fun loadChapterPages(chapterId: String) {
        _isLoading.value = true
        _currentChapterId.value = chapterId
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
                // Find the chapter name from loaded chapters
                var chapterName = ""
                val chapter = _chapters.value.find { it.id == chapterId }
                if (chapter != null) {
                    chapterName = chapter.attributes.chapter ?: ""
                }
                
                val success = firebaseRepository.addToReadingHistory(
                    com.example.apptruyencopy.model.ReadingHistory(
                        mangaId = mangaId,
                        chapterId = chapterId,
                        chapterName = chapterName,
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
    
    // Load all chapters for a manga
    fun loadMangaChapters(mangaId: String, primaryLanguage: String = "vi") {
        viewModelScope.launch {
            try {
                // First clear the current chapters list
                _chapters.value = emptyList()
                
                // Try primary language first
                var chapterList = repository.getChapterList(mangaId, primaryLanguage)
                
                // If no chapters found in primary language, try English as fallback
                if (chapterList.isEmpty() && primaryLanguage != "en") {
                    println("DEBUG: No chapters found in $primaryLanguage, trying 'en'")
                    chapterList = repository.getChapterList(mangaId, "en")
                }
                
                // If still no chapters, try with no language filter
                if (chapterList.isEmpty()) {
                    println("DEBUG: Trying to load all chapters regardless of language")
                    // This is a workaround - we'd need to modify the API to support this properly
                    try {
                        // Try to get chapters in other languages if the API supports it
                        // For now, we'll just use what we have
                    } catch (e: Exception) {
                        println("DEBUG: Failed to load chapters without language filter: ${e.message}")
                    }
                }
                
                if (chapterList.isNotEmpty()) {
                    // Sort chapters by chapter number
                    _chapters.value = chapterList.sortedBy { it.attributes.chapter?.toFloatOrNull() ?: 0f }
                    
                    // Find current chapter index and log it for debugging
                    val index = _chapters.value.indexOfFirst { it.id == _currentChapterId.value }
                    _currentChapterIndex.value = index
                    
                    // Log for debugging
                    println("DEBUG: Loaded ${_chapters.value.size} chapters")
                    println("DEBUG: Current chapter ID: ${_currentChapterId.value}")
                    println("DEBUG: Current chapter index: $index")
                } else {
                    println("DEBUG: No chapters found for manga $mangaId in any language")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("DEBUG: Error loading chapters - ${e.message}")
                _chapters.value = emptyList()
                _currentChapterIndex.value = -1
            }
        }
    }
    
    // Check if there is a previous chapter
    fun hasPreviousChapter(): Boolean {
        return _currentChapterIndex.value > 0
    }
    
    // Check if there is a next chapter
    fun hasNextChapter(): Boolean {
        return _currentChapterIndex.value >= 0 && _currentChapterIndex.value < _chapters.value.size - 1
    }
    
    // Get the next chapter ID
    fun getNextChapterId(): String? {
        return if (hasNextChapter()) {
            _chapters.value[_currentChapterIndex.value + 1].id
        } else {
            null
        }
    }
    
    // Get the previous chapter ID
    fun getPreviousChapterId(): String? {
        return if (hasPreviousChapter()) {
            _chapters.value[_currentChapterIndex.value - 1].id
        } else {
            null
        }
    }
    
    // Get current chapter number
    fun getCurrentChapterNumber(): String {
        return if (_currentChapterIndex.value >= 0 && _currentChapterIndex.value < _chapters.value.size) {
            _chapters.value[_currentChapterIndex.value].attributes.chapter ?: "?"
        } else {
            "?"
        }
    }
} 