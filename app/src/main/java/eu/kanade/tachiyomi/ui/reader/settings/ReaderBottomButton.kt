package eu.kanade.tachiyomi.ui.reader.settings

import androidx.annotation.StringRes
import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.R
import yokai.i18n.MR
import yokai.util.lang.getString
import dev.icerock.moko.resources.compose.stringResource

enum class ReaderBottomButton(val value: String, val stringRes: StringResource) {
    ViewChapters("vc", MR.strings.view_chapters),
    WebView("wb", MR.strings.open_in_webview),
    ReadingMode("rm", MR.strings.reading_mode),
    Rotation("rot", MR.strings.rotation),
    CropBordersPaged("cbp", MR.strings.crop_borders_paged),
    CropBordersWebtoon("cbw", MR.strings.crop_borders_long_strip),
    PageLayout("pl", MR.strings.page_layout),
    ShiftDoublePage("sdp", MR.strings.shift_double_pages),

    ;

    fun isIn(buttons: Collection<String>) = value in buttons

    companion object {
        val BUTTONS_DEFAULTS = setOf(
            ViewChapters,
            WebView,
            PageLayout,
            CropBordersWebtoon,
        ).map { it.value }.toSet()
    }
}
