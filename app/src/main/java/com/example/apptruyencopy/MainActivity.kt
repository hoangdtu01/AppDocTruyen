package com.example.apptruyencopy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.apptruyencopy.ui.*
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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

        // Updated reader route with needed parameters for history
        composable(
            route = "reader/{chapterId}/{mangaId}/{title}/{coverUrl}",
            arguments = listOf(
                navArgument("chapterId") { type = NavType.StringType },
                navArgument("mangaId") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("coverUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
            val mangaId = backStackEntry.arguments?.getString("mangaId") ?: ""
            val encodedTitle = backStackEntry.arguments?.getString("title") ?: ""
            val encodedCoverUrl = backStackEntry.arguments?.getString("coverUrl") ?: ""
            
            // Decode URL-encoded strings
            val title = URLDecoder.decode(encodedTitle, StandardCharsets.UTF_8.toString())
            val coverUrl = URLDecoder.decode(encodedCoverUrl, StandardCharsets.UTF_8.toString())
            
            ReaderScreen(
                chapterId = chapterId,
                mangaId = mangaId,
                title = title,
                coverUrl = coverUrl,
                navController = navController
            )
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

        // Màn hình yêu thích
        composable("favorites") {
            FavoritesScreen(navController)
        }
    }
}