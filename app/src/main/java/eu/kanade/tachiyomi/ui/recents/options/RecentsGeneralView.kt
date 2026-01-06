package eu.kanade.tachiyomi.ui.recents.options

import android.content.Context
import android.util.AttributeSet
import eu.kanade.tachiyomi.R
import yokai.i18n.MR
import yokai.util.lang.getString
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.databinding.RecentsGeneralViewBinding
import eu.kanade.tachiyomi.util.bindToPreference
import eu.kanade.tachiyomi.util.lang.withSubtitle
import eu.kanade.tachiyomi.widget.BaseRecentsDisplayView

class RecentsGeneralView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    BaseRecentsDisplayView<RecentsGeneralViewBinding>(context, attrs) {

    override fun inflateBinding() = RecentsGeneralViewBinding.bind(this)
    override fun initGeneralPreferences() {
        val titleText = context.getString(MR.strings.show_reset_history_button)
        val uniformText = context.getString(MR.strings.uniform_covers)
        binding.showRemoveHistory.text = titleText
            .withSubtitle(binding.showRemoveHistory.context, MR.strings.press_and_hold_to_also_reset)
        binding.uniformCovers.text = uniformText
            .withSubtitle(binding.uniformCovers.context, MR.strings.affects_library_grid)
        binding.showRecentsDownload.bindToPreference(recentsPreferences.showRecentsDownloads())
        binding.showRemoveHistory.bindToPreference(recentsPreferences.showRecentsRemHistory())
        binding.showReadInAll.bindToPreference(recentsPreferences.showReadInAllRecents())
        binding.showTitleFirst.bindToPreference(recentsPreferences.showTitleFirstInRecents())
        binding.uniformCovers.bindToPreference(uiPreferences.uniformGrid())
        binding.outlineOnCovers.bindToPreference(uiPreferences.outlineOnCovers())
    }
}
