package yokai.domain.category.interactor

import eu.kanade.tachiyomi.data.database.models.MangaCategory
import yokai.domain.manga.MangaRepository

class SetMangaCategories(
    private val mangaRepository: MangaRepository,
) {
    suspend fun await(mangaId: Long?, categories: List<Long>) {
        mangaRepository.setCategories(mangaId ?: return, categories)
    }
    suspend fun awaitAll(mangaIds: List<Long>, mangaCategories: List<MangaCategory>) =
        mangaRepository.setMultipleMangaCategories(mangaIds, mangaCategories)
}
