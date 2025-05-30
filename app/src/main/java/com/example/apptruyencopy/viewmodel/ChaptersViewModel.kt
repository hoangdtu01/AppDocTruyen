package com.example.apptruyencopy.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptruyencopy.model.Chapter
import com.example.apptruyencopy.model.Comment
import com.example.apptruyencopy.model.Manga
import com.example.apptruyencopy.model.Rating
import com.example.apptruyencopy.repository.FirebaseRepository
import com.example.apptruyencopy.repository.MangaRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    
    // Rating states
    private val _ratings = mutableStateOf<List<Rating>>(emptyList())
    val ratings: State<List<Rating>> = _ratings
    
    private val _isRatingsLoading = mutableStateOf(false)
    val isRatingsLoading: State<Boolean> = _isRatingsLoading
    
    private val _userRating = mutableStateOf<Rating?>(null)
    val userRating: State<Rating?> = _userRating
    
    private val _ratingScore = mutableStateOf(0f)
    val ratingScore: State<Float> = _ratingScore
    
    private val _ratingReview = mutableStateOf("")
    val ratingReview: State<String> = _ratingReview
    
    private val _averageRating = mutableStateOf(0f)
    val averageRating: State<Float> = _averageRating
    
    val languageOptions = mapOf("vi" to "Tiếng Việt", "en" to "Tiếng Anh")
    
    fun loadMangaDetail(mangaId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val mangaDetail = repository.getMangaDetail(mangaId)
                _manga.value = mangaDetail
                
                // Load English chapters for count
                val enChapters = repository.getChapterList(mangaId, "en")
                _totalEnChapters.value = enChapters.size
                
                // Check if manga is in user's favorites
                if (auth.currentUser != null) {
                    val favorites = firebaseRepository.getFavoriteMangas(auth.currentUser!!.uid)
                    _isFavorite.value = favorites.contains(mangaId)
                }
                
                // Load comments
                loadComments(mangaId)
                
                // Load ratings
                loadRatings(mangaId)
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isLoading.value = false
        }
    }


    /*
        * Toggle favorite status for the manga.
        * If the user is logged in, it will add or remove the manga from their favorites.
     */
    fun toggleFavorite(mangaId: String) {
        if (auth.currentUser == null) return
        
        viewModelScope.launch {
            try {
                if (_isFavorite.value) {
                    firebaseRepository.removeFromFavorites(mangaId)
                } else {
                    firebaseRepository.addToFavorites(mangaId)
                }
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
    
    // Rating related functions
    fun loadRatings(mangaId: String) {
        _isRatingsLoading.value = true
        viewModelScope.launch {
            try {
                // Get all ratings
                val ratingsList = firebaseRepository.getRatings(mangaId)
                _ratings.value = ratingsList
                
                // Get average rating
                _averageRating.value = firebaseRepository.getAverageRating(mangaId)
                
                // Get user's rating if they're logged in
                if (auth.currentUser != null) {
                    val userRating = firebaseRepository.getUserRating(mangaId)
                    _userRating.value = userRating
                    
                    // Set current rating values if user has rated
                    userRating?.let {
                        _ratingScore.value = it.score
                        _ratingReview.value = it.review
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isRatingsLoading.value = false
        }
    }
    
    fun updateRatingScore(score: Float) {
        _ratingScore.value = score
    }
    
    fun updateRatingReview(review: String) {
        _ratingReview.value = review
    }
    
    fun submitRating(mangaId: String) {
        if (auth.currentUser == null) return
        
        viewModelScope.launch {
            try {
                firebaseRepository.addRating(
                    mangaId = mangaId,
                    score = _ratingScore.value,
                    review = _ratingReview.value
                )
                
                // Reload ratings to show the new one
                loadRatings(mangaId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun deleteRating(ratingId: String, mangaId: String) {
        if (auth.currentUser == null) return
        
        viewModelScope.launch {
            try {
                firebaseRepository.deleteRating(ratingId)
                
                // Reset user rating values
                _ratingScore.value = 0f
                _ratingReview.value = ""
                _userRating.value = null
                
                // Reload ratings to reflect the deletion
                loadRatings(mangaId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // Helper method to get the rating count by integer score (1-5)
    fun getRatingCountsByScore(): Map<Int, Int> {
        val ratingsByScore = mutableMapOf<Int, Int>()
        
        for (rating in _ratings.value) {
            // Convert float score (0-5) to integer score (1-5)
            val score = convertRatingToStarCategory(rating.score)
            ratingsByScore[score] = (ratingsByScore[score] ?: 0) + 1
        }
        
        return ratingsByScore
    }
    
    // Convert a raw rating score to a star category (1-5)
    private fun convertRatingToStarCategory(score: Float): Int {
        return when {
            score >= 4.5f -> 5
            score >= 3.5f -> 4
            score >= 2.5f -> 3
            score >= 1.5f -> 2
            else -> 1
        }
    }
} 