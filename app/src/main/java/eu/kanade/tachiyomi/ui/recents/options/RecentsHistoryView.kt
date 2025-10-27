package eu.kanade.tachiyomi.ui.recents.options

import android.content.Context
import android.util.AttributeSet
import eu.kanade.tachiyomi.R
import yokai.i18n.MR
import yokai.util.lang.getString
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.databinding.RecentsHistoryViewBinding
import eu.kanade.tachiyomi.util.bindToPreference
import eu.kanade.tachiyomi.util.system.materialAlertDialog
import eu.kanade.tachiyomi.util.view.setMessage
import eu.kanade.tachiyomi.util.view.setPositiveButton
import eu.kanade.tachiyomi.widget.BaseRecentsDisplayView
import android.R as AR

class RecentsHistoryView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    BaseRecentsDisplayView<RecentsHistoryViewBinding>(context, attrs) {

    override fun inflateBinding() = RecentsHistoryViewBinding.bind(this)
    override fun initGeneralPreferences() {
        binding.groupChapters.bindToPreference(preferences.groupChaptersHistory())
        binding.collapseGroupedChapters.bindToPreference(preferences.collapseGroupedHistory()) {
            controller?.presenter?.expandedSectionsMap?.clear()
        }
        binding.clearHistory.setOnClickListener {
            val activity = controller?.activity ?: return@setOnClickListener
            activity.materialAlertDialog()
                .setMessage(MR.strings.clear_history_confirmation)
                .setPositiveButton(MR.strings.clear) { _, _ ->
                    controller?.presenter?.deleteAllHistory()
                }
                .setNegativeButton(AR.string.cancel, null)
                .show()
        }
    }
}
