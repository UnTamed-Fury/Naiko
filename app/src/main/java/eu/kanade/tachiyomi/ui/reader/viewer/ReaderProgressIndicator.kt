package eu.kanade.tachiyomi.ui.reader.viewer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.annotation.IntRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.isVisible
import yokai.presentation.component.CombinedCircularProgressIndicator
import yokai.presentation.theme.YokaiTheme

/**
 * A custom progress bar that always rotates while being determinate. By always rotating we give
 * the feedback to the user that the application isn't 'stuck', and by making it determinate the
 * user also approximately knows how much the operation will take.
 */
class ReaderProgressIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AbstractComposeView(context, attrs, defStyleAttr) {

    private var progress by mutableFloatStateOf(0f)
    private var isInvertedFromTheme by mutableStateOf(false)

    fun setInvertMode(value: Boolean) {
        isInvertedFromTheme = value
    }

    init {
        layoutParams = FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER)
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
    }

    @Composable
    override fun Content() {
        YokaiTheme {
            CombinedCircularProgressIndicator(progress = { progress }, isInverted = { isInvertedFromTheme })
        }
    }

    fun show() {
        isVisible = true
    }

    /**
     * Hides this progress bar with an optional fade out if [animate] is true.
     */
    fun hide(animate: Boolean = false) {
        if (!isVisible) return

        if (!animate) {
            isVisible = false
        } else {
            ObjectAnimator.ofFloat(this, "alpha", 1f, 0f).apply {
                interpolator = DecelerateInterpolator()
                duration = 1000
                addListener(
                    object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            isVisible = false
                            alpha = 1f
                        }

                        override fun onAnimationCancel(animation: Animator) {
                            alpha = 1f
                        }
                    },
                )
                start()
            }
        }
    }

    /**
     * Completes this progress bar and fades out the view.
     */
    fun completeAndFadeOut() {
        setProgress(100)
        hide(true)
    }

    fun setProgress(@IntRange(from = 0, to = 100) progress: Int) {
        this.progress = progress / 100f
    }
}
