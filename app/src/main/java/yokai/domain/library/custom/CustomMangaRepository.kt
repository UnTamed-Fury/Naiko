package yokai.domain.library.custom

import kotlinx.coroutines.flow.Flow
import yokai.domain.library.custom.model.CustomMangaInfo

interface CustomMangaRepository {
    fun subscribeAll(): Flow<List<CustomMangaInfo>>
    suspend fun getAll(): List<CustomMangaInfo>
    suspend fun insertCustomManga(
        mangaId: Long,
        title: String? = null,
        author: String? = null,
        artist: String? = null,
        description: String? = null,
        genre: String? = null,
        status: Int? = null,
    )
    suspend fun insertCustomManga(mangaInfo: CustomMangaInfo) =
        insertCustomManga(
            mangaInfo.mangaId,
            mangaInfo.title,
            mangaInfo.author,
            mangaInfo.artist,
            mangaInfo.description,
            mangaInfo.genre,
            mangaInfo.status,
        )
    suspend fun insertBulkCustomManga(mangaList: List<CustomMangaInfo>)
    suspend fun deleteCustomManga(mangaId: Long)
    suspend fun deleteBulkCustomManga(mangaIds: List<Long>)
    suspend fun relinkCustomManga(oldId: Long, newId: Long)
}
