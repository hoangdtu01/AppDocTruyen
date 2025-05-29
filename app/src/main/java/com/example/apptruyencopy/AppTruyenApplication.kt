package com.example.apptruyencopy

import android.app.Application
import com.google.firebase.FirebaseApp

class AppTruyenApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Khởi tạo Firebase
        FirebaseApp.initializeApp(this)
    }
} 