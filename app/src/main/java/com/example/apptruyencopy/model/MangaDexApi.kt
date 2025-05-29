package com.example.apptruyencopy.model

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MangaDexApi {
    @GET("manga")
    suspend fun getMangaList(
        @Query("limit") limit: Int = 8,
        @Query("includes[]") includes: List<String> = listOf("cover_art")
    ): MangaResponse

    @GET("manga")
    suspend fun getTopRatedMangas(
        @Query("limit") limit: Int = 10,
        @Query("includes[]") includes: List<String> = listOf("cover_art"),
        @Query("order[rating]") order: String = "desc"
    ): MangaResponse

    @GET("manga")
    suspend fun getRecentlyUpdatedMangas(
        @Query("limit") limit: Int = 10,
        @Query("includes[]") includes: List<String> = listOf("cover_art"),
        @Query("order[updatedAt]") order: String = "desc"
    ): MangaResponse

    @GET("manga")
    suspend fun searchManga(
        @Query("title") title: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("includes[]") includes: List<String> = listOf("cover_art")
    ): MangaResponse

    @GET("manga/tag")
    suspend fun getAllGenres(): GenreResponse

    @GET("manga")
    suspend fun getMangaByGenres(
        @Query("includedTags[]") genreIds: List<String>,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0,
        @Query("includes[]") includes: List<String> = listOf("cover_art"),
        @Query("availableTranslatedLanguage[]") language: String = "en",
        @Query("order[followedCount]") order: String = "desc"
    ): MangaResponse


    @GET("manga/{id}")
    suspend fun getMangaDetail(
        @Path("id") mangaId: String,
        @Query("includes[]") includes: List<String> = listOf("author", "artist", "cover_art")
    ): MangaSingleResponse

    @GET("manga/{id}/feed")
    suspend fun getChapterList(
        @Path("id") mangaId: String,
        @Query("translatedLanguage[]") language: String,
        @Query("order[chapter]") order: String = "asc",
        @Query("limit") limit: Int = 100
    ): ChapterResponse

    @GET("chapter/{id}")
    suspend fun getChapter(@Path("id") chapterId: String): ChapterDetailResponse
    
    @GET("chapter")
    suspend fun getChapterCountOnly(
        @Query("manga") mangaId: String,
        @Query("translatedLanguage[]") language: String,
        @Query("limit") limit: Int = 1 // Chỉ cần trả về để lấy `total`
    ): ChapterResponse

    @GET("at-home/server/{chapterId}")
    suspend fun getChapterPages(@Path("chapterId") chapterId: String): ChapterPagesResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://api.mangadex.org/"
    
    val mangaDexApi: MangaDexApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MangaDexApi::class.java)
    }
} 