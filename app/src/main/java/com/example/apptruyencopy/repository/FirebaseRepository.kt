package com.example.apptruyencopy.repository

import com.example.apptruyencopy.model.Comment
import com.example.apptruyencopy.model.FirebaseUser
import com.example.apptruyencopy.model.ReadingHistory
import com.google.firebase.Timestamp
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
    
    // Check if user exists and create if not
    private suspend fun ensureUserExists(userId: String): Boolean {
        val userDoc = db.collection("users").document(userId).get().await()
        
        if (!userDoc.exists()) {
            // Create a basic user record if it doesn't exist
            val newUser = FirebaseUser(
                userId = userId,
                name = auth.currentUser?.displayName ?: "",
                email = auth.currentUser?.email ?: ""
            )
            
            try {
                db.collection("users")
                    .document(userId)
                    .set(newUser)
                    .await()
                return true
            } catch (e: Exception) {
                println("Error creating user document: ${e.message}")
                return false
            }
        }
        
        return true
    }

    // Reading History operations
    // Improved addToReadingHistory with duplicate prevention and user check
    suspend fun addToReadingHistory(history: ReadingHistory): Boolean {
        val userId = auth.currentUser?.uid ?: return false

        try {
            // Ensure user document exists before trying to add history
            if (!ensureUserExists(userId)) {
                println("Failed to ensure user exists, cannot save history")
                return false
            }
            
            // Check if there's already an entry for this manga
            val existingHistory = db.collection("users")
                .document(userId)
                .collection("readingHistory")
                .whereEqualTo("mangaId", history.mangaId)
                .get()
                .await()

            // If entry exists, update it instead of creating a new one
            if (!existingHistory.isEmpty) {
                val docId = existingHistory.documents.first().id
                db.collection("users")
                    .document(userId)
                    .collection("readingHistory")
                    .document(docId)
                    .set(history.copy(timestamp = Timestamp.now()))
                    .await()
            } else {
                // Add new entry
                db.collection("users")
                    .document(userId)
                    .collection("readingHistory")
                    .add(history)
                    .await()

                // Optional: Limit history entries (e.g., keep only last 100)
                limitHistoryEntries(userId, 100)
            }
            return true
        } catch (e: Exception) {
            println("Error saving reading history: ${e.message}")
            return false
        }
    }

    // Helper method to limit the number of history entries
    private suspend fun limitHistoryEntries(userId: String, maxEntries: Int) {
        try {
            val allHistory = db.collection("users")
                .document(userId)
                .collection("readingHistory")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            if (allHistory.size() > maxEntries) {
                val toDelete = allHistory.documents
                    .drop(maxEntries)
                    .map { it.id }

                for (docId in toDelete) {
                    db.collection("users")
                        .document(userId)
                        .collection("readingHistory")
                        .document(docId)
                        .delete()
                        .await()
                }
            }
        } catch (e: Exception) {
            println("Error limiting history entries: ${e.message}")
        }
    }

    // Add a method to delete a single history entry
    suspend fun deleteHistoryEntry(historyId: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false

        return try {
            db.collection("users")
                .document(userId)
                .collection("readingHistory")
                .document(historyId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Add a method to clear all history
    suspend fun clearReadingHistory(): Boolean {
        val userId = auth.currentUser?.uid ?: return false

        return try {
            val batch = db.batch()
            val historyDocs = db.collection("users")
                .document(userId)
                .collection("readingHistory")
                .get()
                .await()

            for (doc in historyDocs) {
                batch.delete(doc.reference)
            }

            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
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
        
        // Ensure user exists before updating favorites
        if (!ensureUserExists(userId)) return
        
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
            user?.favoriteManga ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Comment operations
    suspend fun addComment(mangaId: String, content: String) {
        val userId = auth.currentUser?.uid ?: return
        val user = getUser(userId)
        val email = user?.email ?: auth.currentUser?.email ?: ""
        val userName = email.split("@").firstOrNull() ?: ""
        
        val comment = Comment(
            mangaId = mangaId,
            userId = userId,
            content = content,
//            userName = user?.name ?: "",
            userName = userName,
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