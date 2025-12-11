package eu.kanade.tachiyomi.ui.anime.episode

import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFlexible
import eu.kanade.tachiyomi.data.database.models.Episode

open class BaseEpisodeAdapter<T : IFlexible<*>>(
    obj: DownloadInterface,
) : FlexibleAdapter<T>(null, obj, true) {

    val baseDelegate = obj

    interface DownloadInterface {
        fun downloadEpisode(position: Int)
        fun startDownloadNow(position: Int)
    }

    interface GroupedDownloadInterface : DownloadInterface {
        fun downloadEpisode(position: Int, episode: Episode)
        fun startDownloadNow(position: Int, episode: Episode)
    }
}
