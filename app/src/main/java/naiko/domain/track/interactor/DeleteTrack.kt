package naiko.domain.track.interactor

import naiko.domain.track.TrackRepository

class DeleteTrack(
    private val trackRepository: TrackRepository,
) {
    suspend fun awaitForManga(mangaId: Long, syncId: Long) = trackRepository.deleteForManga(mangaId, syncId)
}
