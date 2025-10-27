package eu.kanade.tachiyomi.ui.recents

import android.view.View
import eu.kanade.tachiyomi.R
import yokai.i18n.MR
import yokai.util.lang.getString
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.databinding.RecentsFooterItemBinding
import eu.kanade.tachiyomi.ui.manga.chapter.BaseChapterHolder
import eu.kanade.tachiyomi.util.view.setText

class RecentMangaFooterHolder(
    view: View,
    val adapter: RecentMangaAdapter,
) : BaseChapterHolder(view, adapter) {
    private val binding = RecentsFooterItemBinding.bind(view)

    fun bind(recentsType: Int) {
        when (recentsType) {
            RecentMangaHeaderItem.CONTINUE_READING -> {
                binding.title.setText(MR.strings.view_history)
            }
            RecentMangaHeaderItem.NEW_CHAPTERS -> {
                binding.title.setText(MR.strings.view_all_updates)
            }
        }
    }

    override fun onLongClick(view: View?): Boolean {
        super.onLongClick(view)
        return false
    }
}
