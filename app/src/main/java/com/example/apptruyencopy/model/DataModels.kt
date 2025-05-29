package com.example.apptruyencopy.model

// Manga
data class MangaResponse(val data: List<Manga>)
data class MangaSingleResponse(val data: Manga)
data class Manga(
    val id: String,
    val attributes: MangaAttributes,
    val imageUrl: String? = null,
    val relationships: List<Relationship> = emptyList()
)

data class MangaAttributes(
    val title: Map<String, String>,
    val tags: List<Tag> = emptyList(),
    val lastChapter: String? = null,
    val description: Map<String, String>? = null
)

data class Tag(
    val id: String,
    val attributes: TagAttributes
)

data class TagAttributes(
    val name: Map<String, String>,
    val group: String? = null
)

data class RelationshipAttributes(
    val name: String? = null,         // Cho tác giả
    val fileName: String? = null      // Cho ảnh bìa
)

data class Relationship(
    val id: String,
    val type: String,
    val attributes: RelationshipAttributes? = null
)

// Genre response
data class GenreResponse(val data: List<Genre>)
data class Genre(
    val id: String,
    val attributes: GenreAttributes,
    var isSelected: Boolean = false // Để theo dõi trạng thái đã chọn trong UI
)
data class GenreAttributes(
    val name: Map<String, String>,
    val description: Map<String, String> = emptyMap(),
    val group: String
)

// Chapter list
data class ChapterResponse(val data: List<Chapter>, val total: Int = 0)
data class Chapter(
    val id: String,
    val attributes: ChapterAttributes
)

data class ChapterAttributes(
    val chapter: String?
)

// Chapter detail
data class ChapterDetailResponse(val data: ChapterDetail)
data class ChapterDetail(val id: String)

// Dữ liệu trang chapter
data class ChapterPagesResponse(val baseUrl: String, val chapter: ChapterData)
data class ChapterData(val hash: String, val data: List<String>) 