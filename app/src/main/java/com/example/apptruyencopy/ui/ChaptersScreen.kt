package com.example.apptruyencopy.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import kotlinx.coroutines.launch

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
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { 
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            ) 
                        }
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
                    ratingCount = ratings.size,
                    ratingCountsByScore = viewModel.getRatingCountsByScore(),
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
    ratingCount: Int = 0,
    ratingCountsByScore: Map<Int, Int> = emptyMap(),
    onNavigateToRatingTab: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = coverUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                            )
                            
                            // Favorite Button
                            if (isLoggedIn) {
                                FloatingActionButton(
                                    onClick = onToggleFavorite,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(16.dp)
                                        .size(48.dp),
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                                ) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tác giả:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = authors,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Thể loại:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                tags.forEach { tag ->
                                    SuggestionChip(
                                        onClick = { },
                                        label = { Text(text = tag) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        ),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Tổng số chương (EN): $totalEnChapters",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Rating card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Xem toàn bộ đánh giá",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left side - Rating score
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(0.3f)
                                        .padding(end = 16.dp)
                                ) {
                                    Text(
                                        text = if (averageRating > 0f) String.format("%.1f", averageRating) else "0.0",
                                        style = MaterialTheme.typography.displayLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Text(
                                        text = "Sao",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "$ratingCount đánh giá",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                
                                // Right side - Star breakdown
                                Column(
                                    modifier = Modifier.weight(0.7f)
                                ) {
                                    // 5 star rating bar
                                    RatingBar(
                                        starCount = 5, 
                                        ratingCount = ratingCountsByScore.getOrDefault(5, 0),
                                        totalRatings = ratingCount
                                    )
                                    
                                    // 4 star rating bar
                                    RatingBar(
                                        starCount = 4,
                                        ratingCount = ratingCountsByScore.getOrDefault(4, 0),
                                        totalRatings = ratingCount
                                    )
                                    
                                    // 3 star rating bar
                                    RatingBar(
                                        starCount = 3,
                                        ratingCount = ratingCountsByScore.getOrDefault(3, 0),
                                        totalRatings = ratingCount
                                    )
                                    
                                    // 2 star rating bar
                                    RatingBar(
                                        starCount = 2,
                                        ratingCount = ratingCountsByScore.getOrDefault(2, 0),
                                        totalRatings = ratingCount
                                    )
                                    
                                    // 1 star rating bar
                                    RatingBar(
                                        starCount = 1,
                                        ratingCount = ratingCountsByScore.getOrDefault(1, 0),
                                        totalRatings = ratingCount
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Filter buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // "Cực phẩm" filter button
                                FilterChip(
                                    label = "Cực phẩm (${ratingCountsByScore.getOrDefault(5, 0) + ratingCountsByScore.getOrDefault(4, 0)})",
                                    selected = false,
                                    onClick = onNavigateToRatingTab
                                )
                                
                                // "Tạm ổn" filter button
                                FilterChip(
                                    label = "Tạm ổn (${ratingCountsByScore.getOrDefault(3, 0)})",
                                    selected = false,
                                    onClick = onNavigateToRatingTab
                                )
                                
                                // "Chưa ưng lắm" filter button
                                FilterChip(
                                    label = "Chưa ưng lắm (${ratingCountsByScore.getOrDefault(2, 0) + ratingCountsByScore.getOrDefault(1, 0)})",
                                    selected = false,
                                    onClick = onNavigateToRatingTab
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // "Viết đánh giá" button
                            OutlinedButton(
                                onClick = onNavigateToRatingTab,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Viết đánh giá",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Viết đánh giá ~",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }


                    // Description card (if available)
                    manga.attributes.description?.get("en")?.let { description ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Mô tả",
                                    style = MaterialTheme.typography.titleMedium,
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Language selection
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Ngôn ngữ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        languageOptions.forEach { (code, label) ->
                            val isSelected = selectedLanguage == code
                            FilledTonalButton(
                                onClick = { if (!isSelected) onLanguageChange(code) },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isSelected) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = if (isSelected) 
                                        MaterialTheme.colorScheme.onPrimary 
                                    else 
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Chapter list header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Text(
                    text = "Danh sách chương",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Không có chapter ${languageOptions[selectedLanguage]?.lowercase()}",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
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
                            .shadow(2.dp, RoundedCornerShape(8.dp))
                            .clickable { 
                                navController.navigate(
                                    "reader/${chapter.id}/${currentManga.id}/${encodedTitle}/${encodedCoverUrl}"
                                ) 
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Chapter number with circle background
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${chapter.attributes.chapter ?: "?"}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column {
                                Text(
                                    text = "Chapter ${chapter.attributes.chapter ?: "?"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                // Only show chapter numbers in this version since the Chapter model doesn't have a title field
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Nhấn để đọc",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Add comment input field (only if logged in)
        if (isLoggedIn) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
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
                        
                        FloatingActionButton(
                            onClick = { if (commentText.isNotBlank()) onAddComment() },
                            containerColor = if (commentText.isBlank()) 
                                MaterialTheme.colorScheme.surfaceVariant
                            else 
                                MaterialTheme.colorScheme.primary,
                            contentColor = if (commentText.isBlank())
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Gửi bình luận"
                            )
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
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Đăng nhập để bình luận",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        // Comments header
        item {
            Text(
                text = "Bình luận",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Chưa có bình luận nào. Hãy là người đầu tiên bình luận!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(2.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = comment.userName.take(1).uppercase(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = comment.userName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Delete button (only visible to comment author)
                if (currentUserId == comment.userId) {
                    IconButton(onClick = { onDelete() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa bình luận",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
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
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Average rating display
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Đánh giá trung bình",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("%.1f", averageRating),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Star rating display
                        StarRating(
                            rating = averageRating,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "${ratings.size} đánh giá",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
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
                        .padding(16.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (userRating == null) "Đánh giá của bạn" else "Cập nhật đánh giá",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Star rating input
                        StarRatingInput(
                            rating = ratingScore,
                            onRatingChange = onRatingScoreChange,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(vertical = 8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Review text input
                        OutlinedTextField(
                            value = ratingReview,
                            onValueChange = onRatingReviewChange,
                            label = { Text("Đánh giá của bạn (không bắt buộc)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (userRating != null) {
                                OutlinedButton(
                                    onClick = { onDeleteRating(userRating.ratingId) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                    modifier = Modifier.padding(end = 16.dp)
                                ) {
                                    Text(
                                        "Xóa",
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            
                            Button(
                                onClick = onSubmitRating,
                                enabled = ratingScore > 0,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    if (userRating == null) "Gửi đánh giá" else "Cập nhật",
                                    fontWeight = FontWeight.Medium
                                )
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
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Đăng nhập để đánh giá",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        // Rating list header
        item {
            Text(
                text = "Tất cả đánh giá",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Chưa có đánh giá nào. Hãy là người đầu tiên đánh giá!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(2.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rating.userName.take(1).uppercase(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = rating.userName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Delete button (only visible to rating author)
                if (currentUserId == rating.userId) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa đánh giá",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Rating stars
            StarRating(
                rating = rating.score,
                modifier = Modifier.size(24.dp)
            )
            
            // Review content if it exists
            if (rating.review.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
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
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            val starFilled = rating > index
            val starPartiallyFilled = rating > index && rating < index + 1
            
            Box(
                modifier = Modifier.padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                if (starPartiallyFilled) {
                    // Use a dedicated half-star icon for better visual representation
                    Icon(
                        imageVector = Icons.Default.StarHalf,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = modifier
                    )
                } else {
                    Icon(
                        imageVector = if (starFilled) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = modifier
                    )
                }
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
    // Animate the scale of stars
    val animatedScale by animateFloatAsState(
        targetValue = if (rating > 0) 1.05f else 1f,
        animationSpec = tween(200, easing = FastOutSlowInEasing)
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            repeat(5) { index ->
                val isActive = rating > index
                val starScale = if (isActive) animatedScale else 1f
                
                IconButton(
                    onClick = { onRatingChange(index + 1f) },
                    modifier = Modifier.scale(starScale)
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Star ${index + 1}",
                        tint = if (isActive) Color(0xFFFFD700) else Color(0xFFCCCCCC),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        
        // Display the selected rating
        if (rating > 0) {
            Text(
                text = when (rating) {
                    1f -> "Rất tệ"
                    2f -> "Tệ"
                    3f -> "Bình thường"
                    4f -> "Tốt"
                    5f -> "Tuyệt vời"
                    else -> ""
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun RatingBar(
    starCount: Int,
    ratingCount: Int,
    totalRatings: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Star count display
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(32.dp)
        ) {
            Text(
                text = "$starCount",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Progress bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // Calculate percentage
            val percentage = if (totalRatings > 0) {
                ratingCount / totalRatings.toFloat()
            } else 0f
            
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
} 