package eu.kanade.tachiyomi.ui.anime.episode

import android.view.View
import androidx.appcompat.widget.PopupMenu
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.database.models.Episode
import eu.kanade.tachiyomi.data.download.model.Download
import eu.kanade.tachiyomi.ui.base.holder.BaseFlexibleViewHolder
import naiko.i18n.MR
import naiko.util.lang.getString

open class BaseEpisodeHolder(
    view: View,
    private val adapter: BaseEpisodeAdapter<*>,
) : BaseFlexibleViewHolder(view, adapter) {

    init {
        view.findViewById<View>(R.id.download_button)?.setOnClickListener { downloadOrRemoveMenu(it) }
    }

    internal fun downloadOrRemoveMenu(downloadButton: View, extraEpisode: Episode? = null, extraStatus: Download.State? = null) {
        val episode = adapter.getItem(flexibleAdapterPosition) as? BaseEpisodeItem<*, *> ?: return

        val episodeStatus = extraStatus ?: episode.status
        if (episodeStatus == Download.State.NOT_DOWNLOADED || episodeStatus == Download.State.ERROR) {
            if (extraEpisode != null) {
                (adapter.baseDelegate as? BaseEpisodeAdapter.GroupedDownloadInterface)
                    ?.downloadEpisode(flexibleAdapterPosition, extraEpisode)
            } else {
                adapter.baseDelegate.downloadEpisode(flexibleAdapterPosition)
            }
        } else {
            downloadButton.post {
                val popup = PopupMenu(downloadButton.context, downloadButton)
                popup.menuInflater.inflate(R.menu.chapter_download, popup.menu) // Reuse chapter download menu if appropriate

                popup.menu.findItem(R.id.action_start).isVisible = episodeStatus == Download.State.QUEUE

                if (episodeStatus != Download.State.DOWNLOADED) {
                    popup.menu.findItem(R.id.action_delete).title = downloadButton.context.getString(
                        MR.strings.cancel,
                    )
                }

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_delete -> {
                            if (extraEpisode != null) {
                                (adapter.baseDelegate as? BaseEpisodeAdapter.GroupedDownloadInterface)
                                    ?.downloadEpisode(flexibleAdapterPosition, extraEpisode)
                            } else {
                                adapter.baseDelegate.downloadEpisode(flexibleAdapterPosition)
                            }
                        }
                        R.id.action_start -> {
                            if (extraEpisode != null) {
                                (adapter.baseDelegate as? BaseEpisodeAdapter.GroupedDownloadInterface)
                                    ?.startDownloadNow(flexibleAdapterPosition, extraEpisode)
                            } else {
                                adapter.baseDelegate.startDownloadNow(flexibleAdapterPosition)
                            }
                        }
                    }
                    true
                }
                popup.show()
            }
        }
    }
}
