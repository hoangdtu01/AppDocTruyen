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

class FavoritesViewModel(
    private val firebaseRepository: FirebaseRepository,
    private val mangaRepository: MangaRepository
) : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    
    private val _favoriteMangas = mutableStateOf<List<Manga>>(emptyList())
    val favoriteMangas: State<List<Manga>> = _favoriteMangas
    
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    private val _isLoggedIn = mutableStateOf(false)
    val isLoggedIn: State<Boolean> = _isLoggedIn
    
    init {
        _isLoggedIn.value = auth.currentUser != null
    }
    
    fun loadFavoriteMangas() {
        if (auth.currentUser == null) {
            _isLoggedIn.value = false
            return
        }
        
        _isLoggedIn.value = true
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                
                // Get favorite manga IDs
                val favoriteIds = firebaseRepository.getFavoriteMangas(userId)
                
                // Fetch manga details for each ID
                val mangas = mutableListOf<Manga>()
                
                for (id in favoriteIds) {
                    mangaRepository.getMangaDetail(id)?.let { manga ->
                        // Add cover image URL
                        val coverRel = manga.relationships.find { it.type == "cover_art" }
                        val fileName = coverRel?.attributes?.fileName
                        val imageUrl = fileName?.let {
                            "https://uploads.mangadex.org/covers/${manga.id}/$it"
                        }
                        
                        mangas.add(manga.copy(imageUrl = imageUrl))
                    }
                }
                
                _favoriteMangas.value = mangas
            } catch (e: Exception) {
                e.printStackTrace()
                _favoriteMangas.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun removeFavorite(mangaId: String) {
        viewModelScope.launch {
            try {
                firebaseRepository.removeFromFavorites(mangaId)
                // Refresh favorites
                loadFavoriteMangas()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
} 