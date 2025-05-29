package com.example.apptruyencopy.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptruyencopy.model.Chapter
import com.example.apptruyencopy.model.Comment
import com.example.apptruyencopy.model.Manga
import com.example.apptruyencopy.repository.FirebaseRepository
import com.example.apptruyencopy.repository.MangaRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ChaptersViewModel(
    private val repository: MangaRepository,
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    
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
    
    private val _isFavorite = mutableStateOf(false)
    val isFavorite: State<Boolean> = _isFavorite
    
    // Comment states
    private val _comments = mutableStateOf<List<Comment>>(emptyList())
    val comments: State<List<Comment>> = _comments
    
    private val _isCommentsLoading = mutableStateOf(false)
    val isCommentsLoading: State<Boolean> = _isCommentsLoading
    
    private val _commentText = mutableStateOf("")
    val commentText: State<String> = _commentText
    
    val languageOptions = mapOf("vi" to "Tiếng Việt", "en" to "Tiếng Anh")
    
    fun loadMangaDetail(mangaId: String) {
        viewModelScope.launch {
            try {
                val mangaDetail = repository.getMangaDetail(mangaId)
                _manga.value = mangaDetail
                
                // Lấy tổng số chapter tiếng Anh
                val total = repository.getChapterCount(mangaId, "en")
                _totalEnChapters.value = total
                
                // Check if manga is in favorites
                checkFavoriteStatus(mangaId)
                
                // Load comments for this manga
                loadComments(mangaId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun checkFavoriteStatus(mangaId: String) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                val favorites = firebaseRepository.getFavoriteMangas(userId)
                _isFavorite.value = favorites.contains(mangaId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun toggleFavorite(mangaId: String) {
        if (auth.currentUser == null) return
        
        viewModelScope.launch {
            try {
                if (_isFavorite.value) {
                    firebaseRepository.removeFromFavorites(mangaId)
                } else {
                    firebaseRepository.addToFavorites(mangaId)
                }
                // Update the favorite status
                _isFavorite.value = !_isFavorite.value
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
    
    // Comment related functions
    fun loadComments(mangaId: String) {
        _isCommentsLoading.value = true
        viewModelScope.launch {
            try {
                val commentsList = firebaseRepository.getComments(mangaId)
                _comments.value = commentsList
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isCommentsLoading.value = false
        }
    }
    
    fun updateCommentText(text: String) {
        _commentText.value = text
    }
    
    fun addComment(mangaId: String) {
        if (_commentText.value.isBlank() || auth.currentUser == null) return
        
        viewModelScope.launch {
            try {
                firebaseRepository.addComment(mangaId, _commentText.value)
                // Clear the comment text field
                _commentText.value = ""
                // Reload comments to show the new one
                loadComments(mangaId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun deleteComment(commentId: String, mangaId: String) {
        if (auth.currentUser == null) return
        
        viewModelScope.launch {
            try {
                firebaseRepository.deleteComment(commentId)
                // Reload comments to reflect the deletion
                loadComments(mangaId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
} 