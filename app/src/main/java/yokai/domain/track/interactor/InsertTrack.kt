package yokai.domain.track.interactor

import eu.kanade.tachiyomi.data.database.models.Track
import yokai.domain.track.TrackRepository

class InsertTrack(
    private val trackRepository: TrackRepository,
) {
    suspend fun await(track: Track) = trackRepository.insert(track)
    suspend fun awaitBulk(tracks: List<Track>) = trackRepository.insertBulk(tracks)
}
