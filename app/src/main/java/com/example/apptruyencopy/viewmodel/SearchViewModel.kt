package com.example.apptruyencopy.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptruyencopy.model.Manga
import com.example.apptruyencopy.repository.MangaRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(private val repository: MangaRepository) : ViewModel() {
    private val _searchText = mutableStateOf("")
    val searchText: State<String> = _searchText

    private val _isSearching = mutableStateOf(false)
    val isSearching: State<Boolean> = _isSearching

    private val _searchResults = mutableStateOf<List<Manga>>(emptyList())
    val searchResults: State<List<Manga>> = _searchResults

    // Flow để xử lý debounce khi người dùng nhập
    private val searchFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            searchFlow
                .debounce(300) // Đợi 300ms sau mỗi lần nhập
                .filter { it.isNotEmpty() }
                .distinctUntilChanged()
                .onEach { _isSearching.value = true }
                .collect { query ->
                    try {
                        val results = repository.searchManga(query)
                        _searchResults.value = results
                    } catch (e: Exception) {
                        // Xử lý lỗi nếu cần
                    } finally {
                        _isSearching.value = false
                    }
                }
        }
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
        searchFlow.value = text
        
        if (text.isEmpty()) {
            _searchResults.value = emptyList()
        }
    }

    fun clearSearch() {
        _searchText.value = ""
        _searchResults.value = emptyList()
    }
} 