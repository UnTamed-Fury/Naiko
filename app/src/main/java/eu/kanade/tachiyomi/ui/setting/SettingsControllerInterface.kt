package eu.kanade.tachiyomi.ui.setting

import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bluelinelabs.conductor.Controller
import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.ui.base.controller.BaseLegacyController
import eu.kanade.tachiyomi.ui.main.MainActivity
import eu.kanade.tachiyomi.util.view.activityBinding
import yokai.util.lang.getString

interface SettingsControllerInterface {
    fun getTitleRes(): StringResource? = null

    fun getTitle(): String?

    fun getSearchTitle(): String?

    @Suppress("FunctionName")
    fun Controller.__setTitle() {
        var parentController = parentController
        while (parentController != null) {
            if (parentController is BaseLegacyController<*> && parentController.getTitle() != null) {
                return
            }
            parentController = parentController.parentController
        }

        (activity as? AppCompatActivity)?.title = getTitle()
        (activity as? MainActivity)?.searchTitle = getSearchTitle()
        activityBinding?.bigIconLayout?.isVisible = false
    }

    @Suppress("FunctionName")
    fun Controller.__getTitle(): String? = getTitleRes()?.let { activity?.getString(it) }
}
