package com.example.apptruyencopy.repository

import com.example.apptruyencopy.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MangaRepository(private val api: MangaDexApi) {
    
    suspend fun getMangaList(): List<Manga> {
        return try {
            val response = api.getMangaList()
            response.data.map { manga ->
                val coverRel = manga.relationships.find { it.type == "cover_art" }
                val fileName = coverRel?.attributes?.fileName
                val imageUrl = fileName?.let {
                    "https://uploads.mangadex.org/covers/${manga.id}/$it"
                }
                manga.copy(imageUrl = imageUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getTopRatedMangas(): List<Manga> {
        return try {
            val response = api.getTopRatedMangas()
            response.data.map { manga ->
                val coverRel = manga.relationships.find { it.type == "cover_art" }
                val fileName = coverRel?.attributes?.fileName
                val imageUrl = fileName?.let {
                    "https://uploads.mangadex.org/covers/${manga.id}/$it"
                }
                manga.copy(imageUrl = imageUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getRecentlyUpdatedMangas(): List<Manga> {
        return try {
            val response = api.getRecentlyUpdatedMangas()
            response.data.map { manga ->
                val coverRel = manga.relationships.find { it.type == "cover_art" }
                val fileName = coverRel?.attributes?.fileName
                val imageUrl = fileName?.let {
                    "https://uploads.mangadex.org/covers/${manga.id}/$it"
                }
                manga.copy(imageUrl = imageUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getAllGenres(): List<Genre> {
        return try {
            val response = api.getAllGenres()
            // Sắp xếp theo tên tiếng Anh từ A-Z
            response.data.sortedBy { it.attributes.name["en"]?.lowercase() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getMangaByGenres(
        genreIds: List<String>, 
        offset: Int = 0, 
        limit: Int = 10, 
        language: String = "en",
        orderBy: String = "desc"
    ): List<Manga> {
        return try {
            if (genreIds.isEmpty()) {
                return getMangaList()
            }
            
            val response = api.getMangaByGenres(
                genreIds = genreIds, 
                offset = offset, 
                limit = limit,
                language = language,
                order = orderBy
            )
            
            response.data.map { manga ->
                val coverRel = manga.relationships.find { it.type == "cover_art" }
                val fileName = coverRel?.attributes?.fileName
                val imageUrl = fileName?.let {
                    "https://uploads.mangadex.org/covers/${manga.id}/$it"
                }
                manga.copy(imageUrl = imageUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getMangaDetail(mangaId: String): Manga? {
        return try {
            val response = api.getMangaDetail(mangaId)
            response.data
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun getChapterList(mangaId: String, language: String): List<Chapter> {
        return try {
            val response = api.getChapterList(mangaId, language)
            response.data
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getChapterCount(mangaId: String, language: String): Int {
        return try {
            val response = api.getChapterCountOnly(mangaId, language)
            response.total
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
    
    suspend fun getChapterPages(chapterId: String): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getChapterPages(chapterId)
                val baseUrl = response.baseUrl
                val hash = response.chapter.hash
                val data = response.chapter.data
                
                data.map { "$baseUrl/data/$hash/$it" }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    suspend fun searchManga(query: String): List<Manga> {
        return try {
            val response = api.searchManga(
                title = query,
                limit = 20,
                offset = 0,
                includes = listOf("cover_art")
            )
            response.data.map { manga ->
                // Xử lý và thêm URL ảnh bìa vào đối tượng Manga
                val coverFileName = manga.relationships
                    .find { it.type == "cover_art" }
                    ?.attributes
                    ?.fileName
                
                manga.copy(
                    imageUrl = if (coverFileName != null) {
                        "https://uploads.mangadex.org/covers/${manga.id}/$coverFileName"
                    } else null
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
} 