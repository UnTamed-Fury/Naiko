package eu.kanade.tachiyomi.ui.recents

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractHeaderItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.kanade.tachiyomi.R
import naiko.i18n.MR
import naiko.util.lang.getString
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.data.database.models.Episode
import eu.kanade.tachiyomi.data.database.models.EpisodeImpl
import eu.kanade.tachiyomi.data.database.models.AnimeEpisodeHistory
import eu.kanade.tachiyomi.data.download.model.Download
// import eu.kanade.tachiyomi.source.model.Page // Video progress logic is different
import eu.kanade.tachiyomi.ui.anime.episode.BaseEpisodeHolder
import eu.kanade.tachiyomi.ui.anime.episode.BaseEpisodeItem

class RecentAnimeItem(
    val aeh: AnimeEpisodeHistory = AnimeEpisodeHistory.createBlank(),
    episode: Episode = EpisodeImpl(),
    header: AbstractHeaderItem<*>?,
) :
    BaseEpisodeItem<BaseEpisodeHolder, AbstractHeaderItem<*>>(episode, header) {

    var downloadInfo = listOf<DownloadInfo>()

    override fun getLayoutRes(): Int {
        return if (aeh.anime.id == null) {
            R.layout.recents_footer_item
        } else {
            R.layout.recent_anime_item
        }
    }

    override fun createViewHolder(
        view: View,
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
    ): BaseEpisodeHolder {
        return if (aeh.anime.id == null) {
            RecentAnimeFooterHolder(view, adapter as RecentAnimeAdapter)
        } else {
            RecentAnimeHolder(view, adapter as RecentAnimeAdapter)
        }
    }

    override fun isSwipeable(): Boolean {
        return aeh.anime.id != null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is RecentAnimeItem) {
            return if (aeh.anime.id == null) {
                (header as? RecentAnimeHeaderItem)?.recentsType ==
                    (other.header as? RecentAnimeHeaderItem)?.recentsType
            } else {
                episode.id == other.episode.id
            }
        }
        return false
    }

    override fun hashCode(): Int {
        return if (aeh.anime.id == null) {
            -((header as? RecentAnimeHeaderItem)?.recentsType ?: 0).hashCode()
        } else {
            (episode.id ?: 0L).hashCode()
        }
    }

    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
        holder: BaseEpisodeHolder,
        position: Int,
        payloads: MutableList<Any?>?,
    ) {
        if (aeh.anime.id == null) {
            (holder as? RecentAnimeFooterHolder)?.bind((header as? RecentAnimeHeaderItem)?.recentsType ?: 0)
        } else if (episode.id != null) (holder as? RecentAnimeHolder)?.bind(this)
    }

    class DownloadInfo {
        private var _status: Download.State = Download.State.default

        var chapterId: Long? = 0L

        val progress: Int
            get() {
                // Video progress logic here
                return 0
            }

        var status: Download.State
            get() = download?.status ?: _status
            set(value) { _status = value }

        @Transient var download: Download? = null

        val isDownloaded: Boolean
            get() = status == Download.State.DOWNLOADED
    }
}
