package yokai.util

import eu.kanade.tachiyomi.ui.recents.RecentsPresenter

fun limitAndOffset(isEndless: Boolean, isResuming: Boolean, offset: Long): Pair<Long, Long> {
    return when {
        isResuming && isEndless && offset > 0 -> {
            offset to 0L
        }
        isEndless -> {
            RecentsPresenter.ENDLESS_LIMIT.toLong() to offset
        }
        else -> {
            RecentsPresenter.SHORT_LIMIT.toLong() to 0L
        }
    }
}
