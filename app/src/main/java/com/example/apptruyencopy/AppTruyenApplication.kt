package com.example.apptruyencopy

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class AppTruyenApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Khởi tạo Firebase
        FirebaseApp.initializeApp(this)
        // Khởi tạo các instance Firebase khác nếu cần
        FirebaseFirestore.getInstance()
        FirebaseAuth.getInstance()
    }
} 