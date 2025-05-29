package com.example.apptruyencopy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.apptruyencopy.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MangaReaderApp()
        }
    }
}

@Composable
fun MangaReaderApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "splash") {
        // Splash Screen
        composable("splash") {
            SplashScreen(navController)
        }
        
        // Màn hình chính
        composable("home") { 
            HomeScreen(navController) 
        }
        
        // Màn hình chi tiết truyện và đọc truyện
        composable("chapters/{mangaId}") { backStackEntry ->
            val mangaId = backStackEntry.arguments?.getString("mangaId") ?: ""
            ChaptersScreen(navController, mangaId)
        }
        composable("reader/{chapterId}") { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
            ReaderScreen(chapterId)
        }
        
        // Màn hình xác thực
        composable("login") {
            LoginScreen(navController)
        }
        composable("signup") {
            SignupScreen(navController)
        }
        composable("profile") {
            ProfileScreen(navController)
        }
        
        // Thêm màn hình lọc thể loại
        composable("genres") {
            GenresScreen(navController)
        }

        composable("search") {
            SearchScreen(navController)
        }

        composable("history") {
            HistoryScreen(navController)
        }
    }
}