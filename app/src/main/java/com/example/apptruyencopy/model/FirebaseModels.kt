package com.example.apptruyencopy.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class FirebaseUser(
    @DocumentId
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    var favoriteManga: List<String> = emptyList()
)

data class ReadingHistory(
    @DocumentId
    val historyId: String = "",
    val mangaId: String = "",
    val chapterId: String = "",
    val title: String = "",
    val coverUrl: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

data class Comment(
    @DocumentId
    val commentId: String = "",
    val mangaId: String = "",
    val userId: String = "",
    val content: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    // Thêm thông tin người dùng để hiển thị trong UI
    var userName: String = "",
    var userAvatar: String = ""
) 