package yokai.presentation.core.util

import android.content.Context
import android.content.Intent
import yokai.presentation.core.Constants

object IntentCommon {
    fun openManga(context: Context, id: Long?, canReturnToMain: Boolean = false) =
        Intent(context, Class.forName(Constants.SEARCH_ACTIVITY))
            .apply {
                action = if (canReturnToMain) Constants.SHORTCUT_MANGA_BACK else Constants.SHORTCUT_MANGA
                putExtra(Constants.MANGA_EXTRA, id)
            }
}
