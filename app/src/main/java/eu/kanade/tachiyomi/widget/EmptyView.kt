package eu.kanade.tachiyomi.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.isVisible
import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.util.isTablet
import yokai.presentation.component.EmptyScreen
import yokai.presentation.theme.YokaiTheme
import yokai.util.lang.getString

class EmptyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AbstractComposeView(context, attrs, defStyleAttr) {

    private var image by mutableStateOf(Icons.Filled.Download)
    private var message by mutableStateOf("")
    private var actions by mutableStateOf(emptyList<Action>())

    init {
        layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER)
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
    }

    @Composable
    override fun Content() {
        YokaiTheme {
            EmptyScreen(
                image = image,
                message = message,
                isTablet = isTablet(),
                actions = actions,
            )
        }
    }

    /**
     * Hide the information view
     */
    fun hide() {
        this.isVisible = false
    }

    /**
     * Show the information view
     * @param textResource text of information view
     */
    fun show(image: ImageVector, textResource: StringResource, actions: List<Action> = emptyList()) {
        show(image, context.getString(textResource), actions)
    }

    /**
     * Show the information view
     * @param textResource text of information view
     */
    fun show(image: ImageVector, @StringRes textResource: Int, actions: List<Action> = emptyList()) {
        show(image, context.getString(textResource), actions)
    }

    /**
     * Show the information view
     * @param drawable icon of information view
     * @param textResource text of information view
     */
    fun show(image: ImageVector, message: String, actions: List<Action> = emptyList()) {
        this.image = image
        this.message = message
        this.actions = actions
        this.isVisible = true
    }

    data class Action(
        val resId: StringResource,
        val listener: () -> Unit,
    )
}
