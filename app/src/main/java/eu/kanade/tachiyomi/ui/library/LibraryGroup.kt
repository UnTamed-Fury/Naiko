package eu.kanade.tachiyomi.ui.library

import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.R
import yokai.i18n.MR
import yokai.util.lang.getString
import dev.icerock.moko.resources.compose.stringResource

object LibraryGroup {

    const val BY_DEFAULT = 0
    const val BY_TAG = 1
    const val BY_SOURCE = 2
    const val BY_STATUS = 3
    const val BY_TRACK_STATUS = 4
    const val BY_AUTHOR = 6
    const val BY_LANGUAGE = 7
    const val UNGROUPED = 5

    fun groupTypeStringRes(type: Int, hasCategories: Boolean = true): StringResource {
        return when (type) {
            BY_STATUS -> MR.strings.status
            BY_TAG -> MR.strings.tag
            BY_SOURCE -> MR.strings.sources
            BY_TRACK_STATUS -> MR.strings.tracking_status
            BY_AUTHOR -> MR.strings.author
            BY_LANGUAGE -> MR.strings.language
            UNGROUPED -> MR.strings.ungrouped
            else -> if (hasCategories) MR.strings.categories else MR.strings.ungrouped
        }
    }

    fun groupTypeDrawableRes(type: Int): Int {
        return when (type) {
            BY_STATUS -> R.drawable.ic_progress_clock_24dp
            BY_TAG -> R.drawable.ic_style_24dp
            BY_TRACK_STATUS -> R.drawable.ic_sync_24dp
            BY_SOURCE -> R.drawable.ic_browse_24dp
            BY_AUTHOR -> R.drawable.ic_author_24dp
            BY_LANGUAGE -> R.drawable.ic_translate_24dp
            UNGROUPED -> R.drawable.ic_ungroup_24dp
            else -> R.drawable.ic_label_outline_24dp
        }
    }
}
