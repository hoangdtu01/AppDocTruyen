package com.example.apptruyencopy.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.apptruyencopy.model.RetrofitClient
import com.example.apptruyencopy.repository.AuthRepository
import com.example.apptruyencopy.repository.MangaRepository
import com.example.apptruyencopy.viewmodel.AuthViewModel
import com.example.apptruyencopy.viewmodel.ChaptersViewModel
import com.example.apptruyencopy.viewmodel.GenresViewModel
import com.example.apptruyencopy.viewmodel.HomeViewModel
import com.example.apptruyencopy.viewmodel.ReaderViewModel

/**
 * Factory để tạo các ViewModel
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Khởi tạo Repository
        val repository = MangaRepository(RetrofitClient.mangaDexApi)
        val authRepository = AuthRepository()
        
        // Khởi tạo các ViewModel
        initializer {
            HomeViewModel(repository)
        }
        
        initializer {
            ChaptersViewModel(repository)
        }
        
        initializer {
            ReaderViewModel(repository)
        }
        
        initializer {
            AuthViewModel(authRepository)
        }
        
        initializer {
            GenresViewModel(repository)
        }
    }
}

/**
 * Extension function để lấy ViewModel từ ViewModelProvider
 */
inline fun <reified VM : androidx.lifecycle.ViewModel> ViewModelStoreOwner.getViewModel(
    extras: CreationExtras = CreationExtras.Empty
): VM {
    return ViewModelProvider(this, AppViewModelProvider.Factory)[VM::class.java]
} 