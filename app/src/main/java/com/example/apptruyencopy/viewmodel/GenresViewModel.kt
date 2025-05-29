package com.example.apptruyencopy.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptruyencopy.model.Genre
import com.example.apptruyencopy.model.Manga
import com.example.apptruyencopy.repository.MangaRepository
import kotlinx.coroutines.launch

class GenresViewModel(private val repository: MangaRepository) : ViewModel() {
    
    private val _genres = mutableStateOf<List<Genre>>(emptyList())
    val genres: State<List<Genre>> = _genres
    
    private val _filteredGenres = mutableStateOf<List<Genre>>(emptyList())
    val filteredGenres: State<List<Genre>> = _filteredGenres
    
    private val _selectedGenreIds = mutableStateOf<List<String>>(emptyList())
    val selectedGenreIds: State<List<String>> = _selectedGenreIds
    
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    private val _isLoadingMore = mutableStateOf(false)
    val isLoadingMore: State<Boolean> = _isLoadingMore
    
    private val _filteredMangas = mutableStateOf<List<Manga>>(emptyList())
    val filteredMangas: State<List<Manga>> = _filteredMangas
    
    private val _currentOffset = mutableStateOf(0)
    val currentOffset: State<Int> = _currentOffset
    
    private val _hasMoreData = mutableStateOf(true)
    val hasMoreData: State<Boolean> = _hasMoreData
    
    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery
    
    private val ITEMS_PER_PAGE = 10
    
    init {
        loadAllGenres()
    }
    
    fun loadAllGenres() {
        _isLoading.value = true
        viewModelScope.launch {
            val genresList = repository.getAllGenres()
            _genres.value = genresList
            _filteredGenres.value = genresList
            _isLoading.value = false
        }
    }
    
    fun toggleGenreSelection(genreId: String) {
        val currentList = _genres.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == genreId }
        if (index != -1) {
            val genre = currentList[index]
            currentList[index] = genre.copy(isSelected = !genre.isSelected)
            _genres.value = currentList
            
            // Cập nhật danh sách lọc
            _filteredGenres.value = filterGenresBySearchQuery(currentList, _searchQuery.value)
            
            // Cập nhật danh sách ID thể loại đã chọn
            _selectedGenreIds.value = currentList.filter { it.isSelected }.map { it.id }
        }
    }
    
    fun applyGenresFilter() {
        _currentOffset.value = 0
        _hasMoreData.value = true
        _filteredMangas.value = emptyList()
        loadMangasByGenres()
    }
    
    fun loadNextPage() {
        if (_hasMoreData.value && !_isLoadingMore.value) {
            _currentOffset.value += ITEMS_PER_PAGE
            loadMoreMangasByGenres()
        }
    }
    
    fun checkIfShouldLoadMore(lastVisibleItemIndex: Int) {
        // Tải thêm khi người dùng đã cuộn gần đến cuối danh sách
        if (lastVisibleItemIndex >= _filteredMangas.value.size - 3 &&
            _hasMoreData.value && !_isLoadingMore.value && !_isLoading.value) {
            loadNextPage()
        }
    }
    
    private fun loadMangasByGenres() {
        _isLoading.value = true
        viewModelScope.launch {
            val mangas = repository.getMangaByGenres(
                genreIds = _selectedGenreIds.value,
                offset = 0,
                limit = ITEMS_PER_PAGE
            )
            _filteredMangas.value = mangas
            _hasMoreData.value = mangas.size == ITEMS_PER_PAGE
            _isLoading.value = false
        }
    }
    
    private fun loadMoreMangasByGenres() {
        _isLoadingMore.value = true
        viewModelScope.launch {
            val mangas = repository.getMangaByGenres(
                genreIds = _selectedGenreIds.value,
                offset = _currentOffset.value,
                limit = ITEMS_PER_PAGE
            )
            
            if (mangas.isEmpty()) {
                _hasMoreData.value = false
            } else {
                _filteredMangas.value = _filteredMangas.value + mangas
                _hasMoreData.value = mangas.size == ITEMS_PER_PAGE
            }
            _isLoadingMore.value = false
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _filteredGenres.value = filterGenresBySearchQuery(_genres.value, query)
    }
    
    fun clearAllSelections() {
        val currentList = _genres.value.map { it.copy(isSelected = false) }
        _genres.value = currentList
        _filteredGenres.value = filterGenresBySearchQuery(currentList, _searchQuery.value)
        _selectedGenreIds.value = emptyList()
    }
    
    private fun filterGenresBySearchQuery(genresList: List<Genre>, query: String): List<Genre> {
        return if (query.isBlank()) {
            genresList
        } else {
            genresList.filter { genre ->
                val enName = genre.attributes.name["en"]?.lowercase() ?: ""
                enName.contains(query.lowercase())
            }
        }
    }
} 