package com.example.apptruyencopy.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apptruyencopy.model.AuthState
import com.example.apptruyencopy.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _authState = mutableStateOf<AuthState>(AuthState.Initial)
    val authState: State<AuthState> = _authState

    private val _currentUser = mutableStateOf<FirebaseUser?>(null)
    val currentUser: State<FirebaseUser?> = _currentUser

    private val _isLoggedIn = mutableStateOf(false)
    val isLoggedIn: State<Boolean> = _isLoggedIn

    init {
        // Kiểm tra người dùng đã đăng nhập chưa khi khởi tạo ViewModel
        _currentUser.value = repository.currentUser
        _isLoggedIn.value = repository.isUserLoggedIn()
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = repository.login(email, password)
            result.onSuccess { user ->
                _currentUser.value = user
                _isLoggedIn.value = true
                _authState.value = AuthState.Success("Đăng nhập thành công")
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Đăng nhập thất bại")
                _isLoggedIn.value = false
            }
        }
    }

    fun signup(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = repository.signup(email, password)
            result.onSuccess { user ->
                _currentUser.value = user
                _isLoggedIn.value = true
                _authState.value = AuthState.Success("Đăng ký thành công")
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Đăng ký thất bại")
                _isLoggedIn.value = false
            }
        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _isLoggedIn.value = false
        _authState.value = AuthState.Initial
    }

    fun resetState() {
        _authState.value = AuthState.Initial
    }
} 