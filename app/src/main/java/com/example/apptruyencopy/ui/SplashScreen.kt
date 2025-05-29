package com.example.apptruyencopy.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.apptruyencopy.R
import com.example.apptruyencopy.repository.AuthRepository
import com.example.apptruyencopy.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel {
        AuthViewModel(AuthRepository())
    }
) {
    val isLoggedIn by authViewModel.isLoggedIn
    
    LaunchedEffect(Unit) {
        delay(2000) // Hiển thị splash screen trong 2 giây
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.placeholder),
            contentDescription = "App Logo",
            modifier = Modifier.size(200.dp), // có thể thay đổi kích thước tùy ý
            contentScale = ContentScale.Fit
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "App Truyện",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        CircularProgressIndicator()
    }
} 