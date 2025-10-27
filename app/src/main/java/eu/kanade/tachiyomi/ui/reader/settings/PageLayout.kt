package eu.kanade.tachiyomi.ui.reader.settings

import androidx.annotation.StringRes
import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.R
import yokai.i18n.MR
import yokai.util.lang.getString
import dev.icerock.moko.resources.compose.stringResource

enum class PageLayout(
    val value: Int,
    val webtoonValue: Int,
    val stringRes: StringResource,
    private val _fullStringRes: StringResource? = null,
) {
    SINGLE_PAGE(0, 0, MR.strings.single_page),
    DOUBLE_PAGES(1, 2, MR.strings.double_pages),
    AUTOMATIC(2, 3, MR.strings.automatic, MR.strings.automatic_orientation),
    SPLIT_PAGES(3, 1, MR.strings.split_double_pages),
    ;

    val fullStringRes = _fullStringRes ?: stringRes

    companion object {
        fun fromPreference(preference: Int): PageLayout =
            entries.find { it.value == preference } ?: SINGLE_PAGE
    }
}
