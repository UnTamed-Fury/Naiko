package eu.kanade.tachiyomi.data.download.model

import eu.kanade.tachiyomi.data.database.models.Episode
import eu.kanade.tachiyomi.domain.anime.models.Anime
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.animesource.AnimeSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

class AnimeDownload(val source: AnimeSource, val anime: Anime, val episode: Episode) {

    var video: Video? = null

    @Transient
    private val _statusFlow = MutableStateFlow(Download.State.NOT_DOWNLOADED)

    @Transient
    val statusFlow = _statusFlow.asStateFlow()
    var status: Download.State
        get() = _statusFlow.value
        set(status) {
            _statusFlow.value = status
        }

    @Transient
    private val _progressFlow = MutableStateFlow(0)
    
    @Transient
    val progressFlow = _progressFlow.asStateFlow()

    var progress: Int
        get() = _progressFlow.value
        set(value) {
            _progressFlow.value = value
        }

    val totalProgress: Int
        get() = progress
}
