package naiko.domain.manga.interactor

import eu.kanade.tachiyomi.data.database.models.LibraryManga
import kotlinx.coroutines.flow.Flow
import naiko.domain.manga.MangaRepository

class GetLibraryManga(
    private val mangaRepository: MangaRepository,
) {
    suspend fun await(): List<LibraryManga> = mangaRepository.getLibraryManga()
    fun subscribe(): Flow<List<LibraryManga>> = mangaRepository.getLibraryMangaAsFlow()
}
