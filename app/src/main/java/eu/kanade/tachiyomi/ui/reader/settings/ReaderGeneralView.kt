package eu.kanade.tachiyomi.ui.reader.settings

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.isVisible
import eu.kanade.tachiyomi.data.database.models.orientationType
import eu.kanade.tachiyomi.data.database.models.readingModeType
import eu.kanade.tachiyomi.databinding.ReaderGeneralLayoutBinding
import eu.kanade.tachiyomi.ui.reader.ReaderActivity
import eu.kanade.tachiyomi.util.bindToPreference
import eu.kanade.tachiyomi.util.lang.addBetaTag
import eu.kanade.tachiyomi.util.system.DeviceUtil
import eu.kanade.tachiyomi.widget.BaseReaderSettingsView
import yokai.util.lang.getString

class ReaderGeneralView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    BaseReaderSettingsView<ReaderGeneralLayoutBinding>(context, attrs) {

    lateinit var sheet: TabbedReaderSettingsSheet
    override fun inflateBinding() = ReaderGeneralLayoutBinding.bind(this)
    override fun initGeneralPreferences() {
        binding.viewerSeries.onItemSelectedListener = { position ->
            val readingModeType = ReadingModeType.fromSpinner(position)
            (context as ReaderActivity).viewModel.setMangaReadingMode(readingModeType.flagValue)

            val mangaViewer = activity.viewModel.getMangaReadingMode()
            if (mangaViewer == ReadingModeType.LONG_STRIP.flagValue || mangaViewer == ReadingModeType.CONTINUOUS_VERTICAL.flagValue) {
                initWebtoonPreferences()
            } else {
                initPagerPreferences()
            }
        }
        binding.viewerSeries.setSelection(
            (context as? ReaderActivity)?.viewModel?.state?.value?.manga?.readingModeType?.let {
                ReadingModeType.fromPreference(it).prefValue
            } ?: 0,
        )
        binding.rotationMode.onItemSelectedListener = { position ->
            val rotationType = OrientationType.fromSpinner(position)
            (context as ReaderActivity).viewModel.setMangaOrientationType(rotationType.flagValue)
        }
        binding.rotationMode.setSelection(
            (context as ReaderActivity).viewModel.manga?.orientationType?.let {
                OrientationType.fromPreference(it).prefValue
            } ?: 0,
        )

        binding.backgroundColor.setEntries(
            ReaderBackgroundColor.entries
                .map { context.getString(it.stringRes) },
        )
        val selection = ReaderBackgroundColor.indexFromPref(preferences.readerTheme().get())
        binding.backgroundColor.setSelection(selection)
        binding.backgroundColor.onItemSelectedListener = { position ->
            val backgroundColor = ReaderBackgroundColor.entries[position]
            preferences.readerTheme().set(backgroundColor.prefValue)
        }
        binding.showPageNumber.bindToPreference(preferences.showPageNumber())
        binding.fullscreen.bindToPreference(preferences.fullscreen()) {
            updatePrefs()
        }
        binding.cutoutShort.bindToPreference(readerPreferences.cutoutShort())
        binding.cutoutShort.text = binding.cutoutShort.text.toString().addBetaTag(context)
        binding.keepscreen.bindToPreference(preferences.keepScreenOn())
        binding.alwaysShowChapterTransition.bindToPreference(preferences.alwaysShowChapterTransition())

        updatePrefs()
    }

    /**
     * Init the preferences for the webtoon reader.
     */
    private fun initWebtoonPreferences() {
        sheet.updateTabs(true)
    }

    private fun initPagerPreferences() {
        sheet.updateTabs(false)
    }

    private fun updatePrefs() {
        binding.cutoutShort.isVisible =
            DeviceUtil.hasCutout(context as ReaderActivity).ordinal >= DeviceUtil.CutoutSupport.MODERN.ordinal && preferences.fullscreen().get()
    }
}
