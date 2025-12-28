package naiko.domain.manga.interactor

import naiko.domain.manga.MangaRepository
import naiko.domain.manga.models.MangaUpdate

class UpdateManga (
    private val mangaRepository: MangaRepository,
) {
    suspend fun await(update: MangaUpdate) = mangaRepository.update(update)
    suspend fun awaitAll(updates: List<MangaUpdate>) = mangaRepository.updateAll(updates)
}
