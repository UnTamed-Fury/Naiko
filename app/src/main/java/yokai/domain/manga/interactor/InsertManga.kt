package yokai.domain.manga.interactor

import eu.kanade.tachiyomi.domain.manga.models.Manga
import yokai.domain.manga.MangaRepository

class InsertManga (
    private val mangaRepository: MangaRepository,
) {
    suspend fun await(manga: Manga) = mangaRepository.insert(manga)
}
