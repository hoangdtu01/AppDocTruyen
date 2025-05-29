package com.example.apptruyencopy.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptruyencopy.model.Comment
import com.example.apptruyencopy.model.FirebaseUser
import com.example.apptruyencopy.model.ReadingHistory
import com.example.apptruyencopy.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val repository: FirebaseRepository) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    
    private val _currentUser = mutableStateOf<FirebaseUser?>(null)
    val currentUser: State<FirebaseUser?> = _currentUser
    
    private val _readingHistory = mutableStateOf<List<ReadingHistory>>(emptyList())
    val readingHistory: State<List<ReadingHistory>> = _readingHistory
    
    private val _favoriteMangas = mutableStateOf<List<String>>(emptyList())
    val favoriteMangas: State<List<String>> = _favoriteMangas
    
    private val _comments = mutableStateOf<List<Comment>>(emptyList())
    val comments: State<List<Comment>> = _comments
    
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isLoadingMore = mutableStateOf(false)
    val isLoadingMore: State<Boolean> = _isLoadingMore

    private val _hasMoreHistory = mutableStateOf(true)
    val hasMoreHistory: State<Boolean> = _hasMoreHistory

    private val _currentHistoryPage = mutableStateOf(0)
    private val HISTORY_PAGE_SIZE = 20

    init {
        auth.currentUser?.let { firebaseUser ->
            loadUserData(firebaseUser.uid)
        }
    }
    
    private fun loadUserData(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _currentUser.value = repository.getUser(userId)
                loadInitialHistory(userId)
                _favoriteMangas.value = repository.getFavoriteMangas(userId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadInitialHistory(userId: String) {
        _currentHistoryPage.value = 0
        _readingHistory.value = repository.getReadingHistory(userId, 0, HISTORY_PAGE_SIZE)
        _hasMoreHistory.value = _readingHistory.value.size >= HISTORY_PAGE_SIZE
    }

    fun loadMoreHistory() {
        if (!_hasMoreHistory.value || _isLoadingMore.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val nextPage = _currentHistoryPage.value + 1
                val userId = auth.currentUser?.uid ?: return@launch
                
                val nextItems = repository.getReadingHistory(
                    userId,
                    nextPage * HISTORY_PAGE_SIZE,
                    HISTORY_PAGE_SIZE
                )
                
                if (nextItems.isNotEmpty()) {
                    _readingHistory.value = _readingHistory.value + nextItems
                    _currentHistoryPage.value = nextPage
                    _hasMoreHistory.value = nextItems.size >= HISTORY_PAGE_SIZE
                } else {
                    _hasMoreHistory.value = false
                }
            } finally {
                _isLoadingMore.value = false
            }
        }
    }
    
    fun addToReadingHistory(mangaId: String, chapterId: String, title: String, coverUrl: String) {
        viewModelScope.launch {
            val history = ReadingHistory(
                mangaId = mangaId,
                chapterId = chapterId,
                title = title,
                coverUrl = coverUrl
            )
            repository.addToReadingHistory(history)
            // Reload reading history
            auth.currentUser?.let { loadUserData(it.uid) }
        }
    }
    
    fun toggleFavorite(mangaId: String) {
        viewModelScope.launch {
            if (_favoriteMangas.value.contains(mangaId)) {
                repository.removeFromFavorites(mangaId)
            } else {
                repository.addToFavorites(mangaId)
            }
            // Reload favorites
            auth.currentUser?.let { loadUserData(it.uid) }
        }
    }
    
    fun loadComments(mangaId: String) {
        viewModelScope.launch {
            _comments.value = repository.getComments(mangaId)
        }
    }
    
    fun addComment(mangaId: String, content: String) {
        viewModelScope.launch {
            repository.addComment(mangaId, content)
            // Reload comments
            loadComments(mangaId)
        }
    }
    
    fun deleteComment(commentId: String, mangaId: String) {
        viewModelScope.launch {
            repository.deleteComment(commentId)
            // Reload comments
            loadComments(mangaId)
        }
    }
} 