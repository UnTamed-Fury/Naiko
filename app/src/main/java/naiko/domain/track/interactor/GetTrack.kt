package naiko.domain.track.interactor

import naiko.domain.track.TrackRepository

class GetTrack(
    private val trackRepository: TrackRepository,
) {
    suspend fun awaitAllByMangaId(mangaId: Long?) = mangaId?.let { trackRepository.getAllByMangaId(it) } ?: listOf()
}
