package eu.kanade.tachiyomi.ui.source.browse

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFlexible
import eu.kanade.tachiyomi.databinding.SourceFilterSheetBinding
import eu.kanade.tachiyomi.util.system.dpToPx
import eu.kanade.tachiyomi.util.system.rootWindowInsetsCompat
import eu.kanade.tachiyomi.util.view.checkHeightThen
import eu.kanade.tachiyomi.util.view.collapse
import eu.kanade.tachiyomi.util.view.doOnApplyWindowInsetsCompat
import eu.kanade.tachiyomi.widget.E2EBottomSheetDialog
import yokai.domain.source.browse.filter.models.SavedSearch
import yokai.presentation.component.recyclerview.VertPaddingDecoration
import android.R as AR

class SourceFilterSheet(
    val activity: Activity,
    searches: () -> List<SavedSearch> = { emptyList() },
    val onSearchClicked: () -> Unit,
    val onResetClicked: () -> Unit,
    val onSaveClicked: () -> Unit,
    val onSavedSearchClicked: (Long) -> Unit,
    val onDeleteSavedSearchClicked: (Long) -> Unit,
) : E2EBottomSheetDialog<SourceFilterSheetBinding>(activity) {

    val adapter: FlexibleAdapter<IFlexible<*>> = FlexibleAdapter<IFlexible<*>>(null)
        .setDisplayHeadersAtStartUp(true)

    override var recyclerView: RecyclerView? = binding.filtersRecycler

    override fun createBinding(inflater: LayoutInflater) = SourceFilterSheetBinding.inflate(inflater)

    private val savedSearchesAdapter = SavedSearchesAdapter(
        searches = searches,
        onSavedSearchClicked = onSavedSearchClicked,
        onDeleteSavedSearchClicked = onDeleteSavedSearchClicked,
    )

    init {
        binding.searchBtn.setOnClickListener { dismiss() }
        binding.resetBtn.setOnClickListener { onResetClicked() }
        binding.saveBtn.setOnClickListener { onSaveClicked() }

        sheetBehavior.peekHeight = 450.dpToPx
        sheetBehavior.collapse()

        binding.titleLayout.checkHeightThen {
            activity.window.decorView.rootWindowInsetsCompat?.let { setCardViewMax(it) }
        }

        binding.cardView.doOnApplyWindowInsetsCompat { _, insets, _ ->
            binding.cardView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                val fullHeight = activity.window.decorView.height
                matchConstraintMaxHeight = fullHeight - insets.getInsets(systemBars()).top - binding.titleLayout.height - 75.dpToPx
            }
        }

        val attrsArray = intArrayOf(AR.attr.actionBarSize)
        val array = context.obtainStyledAttributes(attrsArray)
        val headerHeight = array.getDimensionPixelSize(0, 0)
        array.recycle()
        binding.root.doOnApplyWindowInsetsCompat { _, insets, _ ->
            binding.titleLayout.updatePaddingRelative(
                bottom = insets.getInsets(systemBars()).bottom,
            )
            binding.titleLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = headerHeight + binding.titleLayout.paddingBottom
            }
            setCardViewMax(insets)
        }

        (binding.root.parent.parent as? View)?.viewTreeObserver?.addOnGlobalLayoutListener(
            object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    updateBottomButtons()
                    if (sheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
                        (binding.root.parent.parent as? View)?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                    }
                }
            },
        )

        recyclerView?.viewTreeObserver?.addOnScrollChangedListener {
            updateBottomButtons()
        }

        setOnShowListener {
            updateBottomButtons()
        }

        recyclerView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView?.addItemDecoration(VertPaddingDecoration(12.dpToPx))
        recyclerView?.adapter = ConcatAdapter(
            savedSearchesAdapter,
            adapter,
        )
        recyclerView?.setHasFixedSize(false)

        sheetBehavior.addBottomSheetCallback(
            object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, progress: Float) {
                    updateBottomButtons()
                }

                override fun onStateChanged(p0: View, state: Int) {
                    updateBottomButtons()
                }
            },
        )
    }

    private fun setCardViewMax(insets: WindowInsetsCompat) {
        val fullHeight = activity.window.decorView.height
        val newHeight = fullHeight - insets.getInsets(systemBars()).top -
            binding.titleLayout.height - 75.dpToPx
        if ((binding.cardView.layoutParams as ConstraintLayout.LayoutParams).matchConstraintMaxHeight != newHeight) {
            binding.cardView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                matchConstraintMaxHeight = newHeight
            }
        }
    }

    override fun onStart() {
        super.onStart()
        sheetBehavior.collapse()
        scrollToTop()  // Force the sheet to scroll to the very top when it shows up
        updateBottomButtons()
        binding.root.post {
            scrollToTop()  // Force the sheet to scroll to the very top when it shows up
            updateBottomButtons()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val attrsArray = intArrayOf(AR.attr.actionBarSize)
        context.withStyledAttributes(null, attrsArray) {
            val headerHeight = getDimensionPixelSize(0, 0)
            binding.titleLayout.updatePaddingRelative(
                bottom = activity.window.decorView.rootWindowInsetsCompat
                    ?.getInsets(systemBars())?.bottom ?: 0,
            )

            binding.titleLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = headerHeight + binding.titleLayout.paddingBottom
            }
        }
    }

    private fun updateBottomButtons() {
        val bottomSheet = binding.root.parent as View
        val bottomSheetVisibleHeight = -bottomSheet.top + (activity.window.decorView.height - bottomSheet.height)

        binding.titleLayout.translationY = bottomSheetVisibleHeight.toFloat()
    }

    override fun dismiss() {
        super.dismiss()
        onSearchClicked()
    }

    fun setFilters(items: List<IFlexible<*>>) {
        adapter.updateDataSet(items)
    }

    fun scrollToTop() {
        recyclerView?.layoutManager?.scrollToPosition(0)
    }
}
