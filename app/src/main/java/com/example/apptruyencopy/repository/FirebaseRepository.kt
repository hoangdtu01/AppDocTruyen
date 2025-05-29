package com.example.apptruyencopy.repository

import com.example.apptruyencopy.model.Comment
import com.example.apptruyencopy.model.FirebaseUser
import com.example.apptruyencopy.model.ReadingHistory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.*

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    // Lưu trữ document snapshot cuối cùng cho pagination
    private var lastHistoryDocument: DocumentSnapshot? = null

    // User operations
    suspend fun createOrUpdateUser(user: FirebaseUser) {
        db.collection("users")
            .document(user.userId)
            .set(user)
            .await()
    }

    suspend fun getUser(userId: String): FirebaseUser? {
        return try {
            db.collection("users")
                .document(userId)
                .get()
                .await()
                .toObject(FirebaseUser::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Reading History operations
    suspend fun addToReadingHistory(history: ReadingHistory) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .collection("readingHistory")
            .add(history)
            .await()
    }

    suspend fun getReadingHistory(userId: String, offset: Int, limit: Int): List<ReadingHistory> {
        return try {
            val query = if (offset == 0) {
                // Trang đầu tiên
                lastHistoryDocument = null
                db.collection("users")
                    .document(userId)
                    .collection("readingHistory")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(limit.toLong())
            } else {
                // Các trang tiếp theo
                lastHistoryDocument?.let { lastDoc ->
                    db.collection("users")
                        .document(userId)
                        .collection("readingHistory")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .startAfter(lastDoc)
                        .limit(limit.toLong())
                } ?: return emptyList()
            }

            val querySnapshot = query.get().await()
            
            // Lưu document cuối cùng cho lần query tiếp theo
            if (querySnapshot.documents.isNotEmpty()) {
                lastHistoryDocument = querySnapshot.documents.last()
            }

            querySnapshot.toObjects(ReadingHistory::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Favorite Manga operations
    suspend fun addToFavorites(mangaId: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .update("favoriteManga", com.google.firebase.firestore.FieldValue.arrayUnion(mangaId))
            .await()
    }

    suspend fun removeFromFavorites(mangaId: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .update("favoriteManga", com.google.firebase.firestore.FieldValue.arrayRemove(mangaId))
            .await()
    }

    suspend fun getFavoriteMangas(userId: String): List<String> {
        return try {
            val user = getUser(userId)
            user?.favoriteMangaIds ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Comment operations
    suspend fun addComment(mangaId: String, content: String) {
        val userId = auth.currentUser?.uid ?: return
        val user = getUser(userId)
        
        val comment = Comment(
            mangaId = mangaId,
            userId = userId,
            content = content,
            userName = user?.name ?: "",
            userAvatar = "" // Thêm avatar URL nếu có
        )
        
        db.collection("comments")
            .add(comment)
            .await()
    }

    suspend fun getComments(mangaId: String): List<Comment> {
        return try {
            db.collection("comments")
                .whereEqualTo("mangaId", mangaId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Comment::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteComment(commentId: String) {
        val userId = auth.currentUser?.uid ?: return
        
        // Kiểm tra xem comment có phải của user hiện tại không
        val comment = db.collection("comments")
            .document(commentId)
            .get()
            .await()
            .toObject(Comment::class.java)
            
        if (comment?.userId == userId) {
            db.collection("comments")
                .document(commentId)
                .delete()
                .await()
        }
    }
} 