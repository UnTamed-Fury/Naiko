package naiko.domain.history.interactor

import naiko.domain.history.HistoryRepository

class GetHistory(
    private val historyRepository: HistoryRepository
) {
    suspend fun awaitByMangaId(mangaId: Long) = historyRepository.getByMangaId(mangaId)
    suspend fun awaitAllByMangaId(mangaId: Long) = historyRepository.getAllByMangaId(mangaId)
}
