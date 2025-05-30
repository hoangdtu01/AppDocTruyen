package com.example.apptruyencopy.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.apptruyencopy.di.AppViewModelProvider
import com.example.apptruyencopy.model.Chapter
import com.example.apptruyencopy.model.Comment
import com.example.apptruyencopy.model.Manga
import com.example.apptruyencopy.model.Rating
import com.example.apptruyencopy.viewmodel.ChaptersViewModel
import com.google.firebase.auth.FirebaseAuth
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    navController: NavController, 
    mangaId: String,
    viewModel: ChaptersViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val manga by viewModel.manga
    val chapters by viewModel.chapters
    val selectedLanguage by viewModel.selectedLanguage
    val isLoading by viewModel.isLoading
    val totalEnChapters by viewModel.totalEnChapters
    val isFavorite by viewModel.isFavorite
    val languageOptions = viewModel.languageOptions
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
    
    // Comment states
    val comments by viewModel.comments
    val isCommentsLoading by viewModel.isCommentsLoading
    val commentText by viewModel.commentText
    
    // Rating states
    val ratings by viewModel.ratings
    val isRatingsLoading by viewModel.isRatingsLoading
    val userRating by viewModel.userRating
    val ratingScore by viewModel.ratingScore
    val ratingReview by viewModel.ratingReview
    val averageRating by viewModel.averageRating
    
    // Tab state
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Giới thiệu", "Chương", "Bình luận", "Đánh giá")

    LaunchedEffect(mangaId) {
        viewModel.loadMangaDetail(mangaId)
        viewModel.loadChapters(mangaId)
    }
    
    // Load comments when comment tab is selected
    LaunchedEffect(selectedTabIndex) {
        when (selectedTabIndex) {
            2 -> viewModel.loadComments(mangaId)
            3 -> viewModel.loadRatings(mangaId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = manga?.attributes?.title?.get("en") 
                            ?: manga?.attributes?.title?.values?.firstOrNull() 
                            ?: "Chi tiết truyện",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            
            // Tab Content
            when (selectedTabIndex) {
                0 -> IntroductionTab(
                    manga = manga,
                    isFavorite = isFavorite,
                    isLoggedIn = isLoggedIn,
                    totalEnChapters = totalEnChapters,
                    onToggleFavorite = { viewModel.toggleFavorite(mangaId) },
                    averageRating = averageRating,
                    onNavigateToRatingTab = { selectedTabIndex = 3 }
                )
                
                1 -> ChaptersTab(
                    isLoading = isLoading,
                    chapters = chapters,
                    selectedLanguage = selectedLanguage,
                    languageOptions = languageOptions,
                    manga = manga,
                    navController = navController,
                    onLanguageChange = { language -> viewModel.changeLanguage(language, mangaId) }
                )
                
                2 -> CommentsTab(
                    isLoggedIn = isLoggedIn,
                    comments = comments,
                    isCommentsLoading = isCommentsLoading,
                    commentText = commentText,
                    onCommentTextChange = viewModel::updateCommentText,
                    onAddComment = { viewModel.addComment(mangaId) },
                    onDeleteComment = { commentId -> viewModel.deleteComment(commentId, mangaId) }
                )
                
                3 -> RatingsTab(
                    isLoggedIn = isLoggedIn,
                    ratings = ratings,
                    isRatingsLoading = isRatingsLoading,
                    userRating = userRating,
                    ratingScore = ratingScore,
                    ratingReview = ratingReview,
                    averageRating = averageRating,
                    onRatingScoreChange = viewModel::updateRatingScore,
                    onRatingReviewChange = viewModel::updateRatingReview,
                    onSubmitRating = { viewModel.submitRating(mangaId) },
                    onDeleteRating = { ratingId -> viewModel.deleteRating(ratingId, mangaId) }
                )
            }
        }
    }
}

@Composable
fun IntroductionTab(
    manga: Manga?,
    isFavorite: Boolean,
    isLoggedIn: Boolean,
    totalEnChapters: Int,
    onToggleFavorite: () -> Unit,
    averageRating: Float = 0f,
    onNavigateToRatingTab: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (manga != null) {
            val title = manga.attributes.title["en"] ?: manga.attributes.title.values.firstOrNull() ?: "No title"
            val authors = manga.relationships.filter { rel -> rel.type == "author" }
                .joinToString { rel -> rel.attributes?.name ?: "Unknown" }

            val tags = manga.attributes.tags.mapNotNull { tag -> tag.attributes.name["en"] }

            val coverRel = manga.relationships.find { rel -> rel.type == "cover_art" }
            val coverFileName = coverRel?.attributes?.fileName

            val coverUrl = coverFileName?.let { fileName ->
                "https://uploads.mangadex.org/covers/${manga.id}/$fileName"
            } ?: ""

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = coverUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                        
                        // Favorite Button
                        if (isLoggedIn) {
                            IconButton(
                                onClick = onToggleFavorite,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tựa: $title", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tác giả: $authors", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Thể loại: ${tags.joinToString()}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tổng số chương (EN): $totalEnChapters", style = MaterialTheme.typography.bodySmall)
                    
                    // Rating information with button to navigate to rating tab
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToRatingTab),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Đánh giá: ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        StarRating(
                            rating = averageRating,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (averageRating > 0) String.format("%.1f", averageRating) else "Chưa có đánh giá",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = onNavigateToRatingTab,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Đánh giá ngay")
                        }
                    }
                    
                    // Description (if available)
                    manga.attributes.description?.get("en")?.let { description ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Mô tả:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun ChaptersTab(
    isLoading: Boolean,
    chapters: List<Chapter>,
    selectedLanguage: String,
    languageOptions: Map<String, String>,
    manga: Manga?,
    navController: NavController,
    onLanguageChange: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Language selection
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                languageOptions.forEach { (code, label) ->
                    Button(
                        onClick = { onLanguageChange(code) },
                        enabled = selectedLanguage != code,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(text = label)
                    }
                }
            }
        }

        // Chapter list header
        item {
            Text(
                text = "Danh sách chương",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        // Chapter list
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (chapters.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Không có chapter ${languageOptions[selectedLanguage]?.lowercase()}")
                }
            }
        } else {
            items(chapters) { chapter ->
                // Gather data needed for reading history
                val currentManga = manga
                if (currentManga != null) {
                    val title = currentManga.attributes.title["en"] 
                        ?: currentManga.attributes.title.values.firstOrNull() 
                        ?: "No title"
                    
                    val coverRel = currentManga.relationships.find { rel -> rel.type == "cover_art" }
                    val coverFileName = coverRel?.attributes?.fileName
                    val coverUrl = coverFileName?.let { fileName ->
                        "https://uploads.mangadex.org/covers/${currentManga.id}/$fileName"
                    } ?: ""
                    
                    // Encode title and coverUrl for navigation
                    val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
                    val encodedCoverUrl = URLEncoder.encode(coverUrl, StandardCharsets.UTF_8.toString())
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { 
                                navController.navigate(
                                    "reader/${chapter.id}/${currentManga.id}/${encodedTitle}/${encodedCoverUrl}"
                                ) 
                            },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Chapter ${chapter.attributes.chapter ?: "?"}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentsTab(
    isLoggedIn: Boolean,
    comments: List<Comment>,
    isCommentsLoading: Boolean,
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onAddComment: () -> Unit,
    onDeleteComment: (String) -> Unit
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Add comment input field (only if logged in)
        if (isLoggedIn) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = onCommentTextChange,
                        placeholder = { Text("Viết bình luận của bạn...") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        maxLines = 3
                    )
                    
                    IconButton(
                        onClick = onAddComment,
                        enabled = commentText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Gửi bình luận",
                            tint = if (commentText.isBlank()) Color.Gray else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Text(
                        "Đăng nhập để bình luận",
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Comments list
        if (isCommentsLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (comments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chưa có bình luận nào. Hãy là người đầu tiên bình luận!")
                }
            }
        } else {
            items(comments) { comment ->
                CommentItem(
                    comment = comment,
                    onDelete = { onDeleteComment(comment.commentId) },
                    currentUserId = currentUserId
                )
            }
        }
        
        // Add some space at the bottom
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    onDelete: () -> Unit,
    currentUserId: String?
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = remember(comment.createdAt) { 
        dateFormat.format(comment.createdAt.toDate()) 
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // User avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = comment.userName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = comment.userName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                // Delete button (only visible to comment author)
                if (currentUserId == comment.userId) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa bình luận",
                            tint = Color.Red.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun RatingsTab(
    isLoggedIn: Boolean,
    ratings: List<Rating>,
    isRatingsLoading: Boolean,
    userRating: Rating?,
    ratingScore: Float,
    ratingReview: String,
    averageRating: Float,
    onRatingScoreChange: (Float) -> Unit,
    onRatingReviewChange: (String) -> Unit,
    onSubmitRating: () -> Unit,
    onDeleteRating: (String) -> Unit
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Average rating display
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Đánh giá trung bình",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("%.1f", averageRating),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Star rating display
                        StarRating(
                            rating = averageRating,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Text(
                        text = "${ratings.size} đánh giá",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
        
        // User rating input
        if (isLoggedIn) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (userRating == null) "Đánh giá của bạn" else "Cập nhật đánh giá",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Star rating input
                        StarRatingInput(
                            rating = ratingScore,
                            onRatingChange = onRatingScoreChange,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Review text input
                        OutlinedTextField(
                            value = ratingReview,
                            onValueChange = onRatingReviewChange,
                            label = { Text("Đánh giá của bạn (không bắt buộc)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (userRating != null) {
                                OutlinedButton(
                                    onClick = { onDeleteRating(userRating.ratingId) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.Red
                                    ),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text("Xóa")
                                }
                            }
                            
                            Button(
                                onClick = onSubmitRating,
                                enabled = ratingScore > 0
                            ) {
                                Text(if (userRating == null) "Gửi đánh giá" else "Cập nhật")
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Text(
                        "Đăng nhập để đánh giá",
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Rating list header
        item {
            Text(
                text = "Tất cả đánh giá",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Ratings list
        if (isRatingsLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (ratings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chưa có đánh giá nào. Hãy là người đầu tiên đánh giá!")
                }
            }
        } else {
            items(ratings) { rating ->
                RatingItem(
                    rating = rating,
                    onDelete = { onDeleteRating(rating.ratingId) },
                    currentUserId = currentUserId
                )
            }
        }
        
        // Add some space at the bottom
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RatingItem(
    rating: Rating,
    onDelete: () -> Unit,
    currentUserId: String?
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = remember(rating.createdAt) { 
        dateFormat.format(rating.createdAt.toDate()) 
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // User avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rating.userName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = rating.userName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                // Delete button (only visible to rating author)
                if (currentUserId == rating.userId) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa đánh giá",
                            tint = Color.Red.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Rating stars
            StarRating(
                rating = rating.score,
                modifier = Modifier.size(20.dp)
            )
            
            // Review content if it exists
            if (rating.review.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = rating.review,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun StarRating(
    rating: Float,
    modifier: Modifier = Modifier
) {
    Row {
        repeat(5) { index ->
            val starFilled = rating > index
            val starPartiallyFilled = rating > index && rating < index + 1
            
            if (starPartiallyFilled) {
                // This would be a partially filled star, but for simplicity we'll use filled stars
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = modifier
                )
            } else {
                Icon(
                    imageVector = if (starFilled) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
fun StarRatingInput(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        repeat(5) { index ->
            IconButton(
                onClick = { onRatingChange(index + 1f) }
            ) {
                Icon(
                    imageVector = if (rating > index) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Star ${index + 1}",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
} 