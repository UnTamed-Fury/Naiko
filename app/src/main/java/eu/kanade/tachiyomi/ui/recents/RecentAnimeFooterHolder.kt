package eu.kanade.tachiyomi.ui.recents

import android.view.View
import eu.kanade.tachiyomi.R
import naiko.i18n.MR
import naiko.util.lang.getString
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.databinding.RecentsFooterItemBinding
import eu.kanade.tachiyomi.ui.anime.episode.BaseEpisodeHolder
import eu.kanade.tachiyomi.util.view.setText

class RecentAnimeFooterHolder(
    view: View,
    val adapter: RecentAnimeAdapter,
) : BaseEpisodeHolder(view, adapter) {
    private val binding = RecentsFooterItemBinding.bind(view)

    fun bind(recentsType: Int) {
        when (recentsType) {
            RecentAnimeHeaderItem.CONTINUE_READING -> {
                binding.title.setText(MR.strings.view_history)
            }
            RecentAnimeHeaderItem.NEW_CHAPTERS -> {
                binding.title.setText(MR.strings.view_all_updates)
            }
        }
    }

    override fun onLongClick(view: View?): Boolean {
        super.onLongClick(view)
        return false
    }
}
