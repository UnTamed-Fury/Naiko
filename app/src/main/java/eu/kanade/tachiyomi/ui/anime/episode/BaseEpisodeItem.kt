package eu.kanade.tachiyomi.ui.anime.episode

import eu.davidea.flexibleadapter.items.AbstractHeaderItem
import eu.davidea.flexibleadapter.items.AbstractSectionableItem
import eu.kanade.tachiyomi.data.database.models.Episode
import eu.kanade.tachiyomi.data.download.model.Download
// import eu.kanade.tachiyomi.source.model.Page // Video progress might be different

abstract class BaseEpisodeItem<T : BaseEpisodeHolder, H : AbstractHeaderItem<*>>(
    val episode: Episode,
    header: H? = null,
) :
    AbstractSectionableItem<T, H?>(header),
    Episode by episode {

    private var _status: Download.State = Download.State.default

    val progress: Int
        get() {
            // Video progress logic might be needed here
            return 0 
        }

    var status: Download.State
        get() = download?.status ?: _status
        set(value) { _status = value }

    @Transient var download: Download? = null

    val isDownloaded: Boolean
        get() = status == Download.State.DOWNLOADED

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is BaseEpisodeItem<*, *>) {
            return episode.id == other.episode.id
        }
        return false
    }

    override fun hashCode(): Int {
        return (episode.id ?: 0L).hashCode()
    }
}
