package eu.kanade.tachiyomi.ui.reader.settings

import dev.icerock.moko.resources.StringResource
import yokai.i18n.MR

enum class ReaderBackgroundColor(val prefValue: Int, val stringRes: StringResource, val longStringRes: StringResource? = null) {
    WHITE(0, MR.strings.white),
    GRAY(4, MR.strings.gray_background),
    BLACK(1, MR.strings.black),
    SMART_PAGE(2, MR.strings.smart_by_page, MR.strings.smart_based_on_page),
    SMART_THEME(3, MR.strings.smart_by_theme, MR.strings.smart_based_on_page_and_theme),
    ;

    val isSmartColor get() = this == SMART_PAGE || this == SMART_THEME
    companion object {
        fun indexFromPref(preference: Int) = entries.indexOf(fromPreference(preference))
        fun fromPreference(preference: Int): ReaderBackgroundColor =
            entries.find { it.prefValue == preference } ?: SMART_PAGE
    }
}
