package eu.kanade.tachiyomi.ui.reader.settings

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import androidx.core.view.isVisible
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.databinding.ReaderPagedLayoutBinding
import eu.kanade.tachiyomi.ui.reader.ReaderActivity
import eu.kanade.tachiyomi.util.bindToPreference
import eu.kanade.tachiyomi.util.lang.addBetaTag
import eu.kanade.tachiyomi.util.system.DeviceUtil
import eu.kanade.tachiyomi.widget.BaseReaderSettingsView
import yokai.i18n.MR
import yokai.util.lang.getString

class ReaderPagedView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    BaseReaderSettingsView<ReaderPagedLayoutBinding>(context, attrs) {

    var needsActivityRecreate = false
    override fun inflateBinding() = ReaderPagedLayoutBinding.bind(this)
    override fun initGeneralPreferences() {
        with(binding) {
            scaleType.bindToPreference(preferences.imageScaleType(), 1) {
                val mangaViewer = (context as? ReaderActivity)?.viewModel?.getMangaReadingMode() ?: 0
                val isWebtoonView = ReadingModeType.isWebtoonType(mangaViewer)
                updatePagedGroup(!isWebtoonView)
                landscapeZoom.isVisible = it == SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE - 1
            }
            binding.navigatePan.bindToPreference(preferences.navigateToPan())
            binding.landscapeZoom.bindToPreference(preferences.landscapeZoom())
            zoomStart.bindToPreference(preferences.zoomStart(), 1)
            cropBorders.bindToPreference(preferences.cropBorders())
            pageTransitions.bindToPreference(preferences.pageTransitions())
            pagerNav.bindToPreference(preferences.navigationModePager())
            pagerInvert.bindToPreference(preferences.pagerNavInverted())
            extendPastCutout.bindToPreference(readerPreferences.pagerCutoutBehavior())
            extendPastCutoutLandscape.title = binding.extendPastCutoutLandscape.title.toString() + "(${context.getString(MR.strings.landscape)})"
            extendPastCutoutLandscape.bindToPreference(readerPreferences.landscapeCutoutBehavior()) {
                needsActivityRecreate = true
            }
            pageLayout.bindToPreference(preferences.pageLayout()) {
                val mangaViewer = (context as? ReaderActivity)?.viewModel?.getMangaReadingMode() ?: 0
                val isWebtoonView = ReadingModeType.isWebtoonType(mangaViewer)
                updatePagedGroup(!isWebtoonView)
            }

            invertDoublePages.bindToPreference(preferences.invertDoublePages())

            pageLayout.title = pageLayout.title.toString().addBetaTag(context)

            val mangaViewer = (context as? ReaderActivity)?.viewModel?.getMangaReadingMode() ?: 0
            val isWebtoonView = ReadingModeType.isWebtoonType(mangaViewer)
            val hasMargins = mangaViewer == ReadingModeType.CONTINUOUS_VERTICAL.flagValue
            cropBordersWebtoon.bindToPreference(if (hasMargins) preferences.cropBorders() else preferences.cropBordersWebtoon())
            webtoonSidePadding.bindToIntPreference(
                preferences.webtoonSidePadding(),
                R.array.webtoon_side_padding_values,
            )
            webtoonEnableZoomOut.bindToPreference(preferences.webtoonEnableZoomOut())
            webtoonEnableDoubleTapZoom.bindToPreference(readerPreferences.webtoonDoubleTapZoomEnabled())
            webtoonNav.bindToPreference(preferences.navigationModeWebtoon())
            webtoonInvert.bindToPreference(preferences.webtoonNavInverted())
            webtoonPageLayout.bindToPreference(preferences.webtoonPageLayout())
            webtoonInvertDoublePages.bindToPreference(preferences.webtoonInvertDoublePages())

            updatePagedGroup(!isWebtoonView)
        }
    }

    fun updatePrefs() {
        val mangaViewer = activity.viewModel.getMangaReadingMode()
        val isWebtoonView = ReadingModeType.isWebtoonType(mangaViewer)
        val hasMargins = mangaViewer == ReadingModeType.CONTINUOUS_VERTICAL.flagValue
        binding.cropBordersWebtoon.bindToPreference(if (hasMargins) preferences.cropBorders() else preferences.cropBordersWebtoon())
        updatePagedGroup(!isWebtoonView)
    }

    private fun updatePagedGroup(show: Boolean) {
        listOf(
            binding.scaleType,
            binding.zoomStart,
            binding.cropBorders,
            binding.pageTransitions,
            binding.pagerNav,
            binding.pagerInvert,
            binding.pageLayout,
            binding.landscapeZoom,
            binding.navigatePan,
        ).forEach { it.isVisible = show }
        listOf(
            binding.cropBordersWebtoon,
            binding.webtoonSidePadding,
            binding.webtoonEnableZoomOut,
            binding.webtoonEnableDoubleTapZoom,
            binding.webtoonNav,
            binding.webtoonInvert,
            binding.webtoonPageLayout,
            binding.webtoonInvertDoublePages,
        ).forEach { it.isVisible = !show }
        val isFullFit = when (preferences.imageScaleType().get()) {
            SubsamplingScaleImageView.SCALE_TYPE_FIT_HEIGHT,
            SubsamplingScaleImageView.SCALE_TYPE_SMART_FIT,
            SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP,
            -> true
            else -> false
        }
        val ogView = (context as? Activity)?.window?.decorView
        binding.landscapeZoom.isVisible = show && preferences.imageScaleType().get() == SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE
        binding.extendPastCutout.isVisible =
            show && isFullFit
                && DeviceUtil.hasCutout(context as? Activity).ordinal >= DeviceUtil.CutoutSupport.LEGACY.ordinal
                && preferences.fullscreen().get()
        binding.extendPastCutoutLandscape.isVisible =
            DeviceUtil.hasCutout(context as? Activity).ordinal >= DeviceUtil.CutoutSupport.MODERN.ordinal
                && preferences.fullscreen().get()
                && ogView?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (binding.extendPastCutoutLandscape.isVisible) {
            binding.filterLinearLayout.removeView(binding.extendPastCutoutLandscape)
            binding.filterLinearLayout.addView(
                binding.extendPastCutoutLandscape,
                binding.filterLinearLayout.indexOfChild(if (show) binding.extendPastCutout else binding.webtoonPageLayout) + 1,
            )
        }
        binding.invertDoublePages.isVisible = show && preferences.pageLayout().get() != PageLayout.SINGLE_PAGE.value
    }
}
