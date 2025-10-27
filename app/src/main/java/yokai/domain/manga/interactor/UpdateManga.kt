package yokai.domain.manga.interactor

import yokai.domain.manga.MangaRepository
import yokai.domain.manga.models.MangaUpdate

class UpdateManga (
    private val mangaRepository: MangaRepository,
) {
    suspend fun await(update: MangaUpdate) = mangaRepository.update(update)
    suspend fun awaitAll(updates: List<MangaUpdate>) = mangaRepository.updateAll(updates)
}
