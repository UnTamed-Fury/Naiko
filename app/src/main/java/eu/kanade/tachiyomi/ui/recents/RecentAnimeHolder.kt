package eu.kanade.tachiyomi.ui.recents

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePaddingRelative
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import coil3.load
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.database.models.Episode
import eu.kanade.tachiyomi.data.database.models.EpisodeHistory
import eu.kanade.tachiyomi.data.download.model.Download
import eu.kanade.tachiyomi.databinding.RecentAnimeItemBinding
import eu.kanade.tachiyomi.databinding.RecentSubEpisodeItemBinding
import eu.kanade.tachiyomi.ui.download.DownloadButton
import eu.kanade.tachiyomi.ui.anime.episode.BaseEpisodeHolder
import eu.kanade.tachiyomi.util.isLocal
import eu.kanade.tachiyomi.util.system.contextCompatColor
import eu.kanade.tachiyomi.util.system.dpToPx
import eu.kanade.tachiyomi.util.system.getResourceColor
import eu.kanade.tachiyomi.util.system.timeSpanFromNow
import eu.kanade.tachiyomi.util.view.setAnimVectorCompat
import eu.kanade.tachiyomi.util.view.setCards
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Date
import java.util.concurrent.TimeUnit
import naiko.i18n.MR
import naiko.util.lang.getString
import android.R as AR

class RecentAnimeHolder(
    view: View,
    val adapter: RecentAnimeAdapter,
) : BaseEpisodeHolder(view, adapter) {

    private val binding = RecentAnimeItemBinding.bind(view)
    var episodeId: Long? = null

    private val isUpdates get() = adapter.viewType.isUpdates
    private val isSmallUpdates get() = isUpdates && !adapter.showUpdatedTime

    private val decimalFormat = DecimalFormat(
        "#.###",
        DecimalFormatSymbols().apply { decimalSeparator = '.' },
    )

    init {
        binding.cardLayout.setOnClickListener { adapter.delegate.onCoverClick(flexibleAdapterPosition) }
        binding.removeHistory.setOnClickListener { adapter.delegate.onRemoveHistoryClicked(flexibleAdapterPosition) }
        binding.showMoreEpisodes.setOnClickListener { _ ->
            val moreVisible = !binding.moreEpisodesLayout.isVisible
            binding.moreEpisodesLayout.isVisible = moreVisible
            adapter.delegate.updateExpandedExtraEpisodes(flexibleAdapterPosition, moreVisible)
            binding.showMoreEpisodes.setAnimVectorCompat(
                if (moreVisible) {
                    R.drawable.anim_expand_more_to_less
                } else {
                    R.drawable.anim_expand_less_to_more
                },
            )
            if (moreVisible) {
                binding.moreEpisodesLayout.children.forEach { view ->
                    RecentSubEpisodeItemBinding.bind(view).updateDivider()
                }
            }
            if (isUpdates && binding.moreEpisodesLayout.children.any { view ->
                !RecentSubEpisodeItemBinding.bind(view).subtitle.text.isNullOrBlank()
            } 
            ) {
                showScanlatorInBody(moreVisible)
            } else {
                addMoreUpdatesText(!moreVisible)
            }
            if (adapter.viewType.isHistory) {
                readLastText(!moreVisible).takeIf { it.isNotEmpty() }
                    ?.let { binding.body.text = it }
            }
            binding.endView.updateLayoutParams<ViewGroup.LayoutParams> {
                height = binding.mainView.height
            }
            val transition = TransitionSet()
                .addTransition(androidx.transition.ChangeBounds())
                .addTransition(androidx.transition.Slide())
            transition.duration =
                itemView.resources.getInteger(AR.integer.config_shortAnimTime).toLong()
            TransitionManager.beginDelayedTransition(adapter.recyclerView, transition)
        }
        updateCards()
        binding.frontView.layoutTransition?.enableTransitionType(LayoutTransition.APPEARING)
    }

    fun updateCards() {
        setCards(adapter.showOutline, binding.card, null)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun bind(item: RecentAnimeItem) {
        val showDLs = adapter.showDownloads
        binding.mainView.transitionName = "recents episode $bindingAdapterPosition transition"
        val showRemoveHistory = adapter.showRemoveHistory
        val showTitleFirst = adapter.showTitleFirst
        binding.downloadButton.downloadButton.isVisible = when (showDLs) {
            RecentAnimeAdapter.ShowRecentsDLs.None -> false
            RecentAnimeAdapter.ShowRecentsDLs.OnlyUnread, RecentAnimeAdapter.ShowRecentsDLs.UnreadOrDownloaded -> !item.episode.seen
            RecentAnimeAdapter.ShowRecentsDLs.OnlyDownloaded -> true
            RecentAnimeAdapter.ShowRecentsDLs.All -> true
        } && !item.aeh.anime.isLocal()

        binding.cardLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
            height = (if (isSmallUpdates) 40 else 80).dpToPx
            width = (if (isSmallUpdates) 40 else 60).dpToPx
        }
        listOf(binding.title, binding.subtitle).forEach {
            it.updateLayoutParams<ConstraintLayout.LayoutParams> {
                if (isSmallUpdates) {
                    if (it == binding.title) topMargin = 5.dpToPx
                    endToStart = R.id.button_layout
                    endToEnd = -1
                } else {
                    if (it == binding.title) topMargin = 2.dpToPx
                    endToStart = -1
                    endToEnd = R.id.main_view
                }
            }
        }
        binding.buttonLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
            if (isSmallUpdates) {
                topToBottom = -1
                topToTop = R.id.card_layout
                bottomToBottom = R.id.card_layout
                topMargin = 4.dpToPx
            } else {
                topToTop = -1
                topToBottom = R.id.subtitle
                bottomToBottom = R.id.main_view
                topMargin = 0
            }
        }
        val freeformCovers = !isSmallUpdates && !adapter.uniformCovers
        with(binding.coverThumbnail) {
            adjustViewBounds = freeformCovers
            scaleType = if (!freeformCovers) ImageView.ScaleType.CENTER_CROP else ImageView.ScaleType.FIT_CENTER
        }
        listOf(binding.coverThumbnail, binding.card).forEach {
            it.updateLayoutParams<ViewGroup.LayoutParams> {
                width = if (!freeformCovers) {
                    ViewGroup.LayoutParams.MATCH_PARENT
                } else {
                    ViewGroup.LayoutParams.WRAP_CONTENT
                }
            }
        }

        binding.removeHistory.isVisible = item.aeh.history.id != null && showRemoveHistory
        val context = itemView.context
        // Simplified preferredEpisodeName logic
        val episodeName = item.episode.name ?: "" // TODO: Implement preferredEpisodeName

        listOf(binding.title, binding.subtitle).forEach {
            it.apply {
                setCompoundDrawablesRelative(null, null, null, null)
                translationX = 0f
                text = if (!showTitleFirst.xor(it === binding.subtitle)) {
                    setTextViewForEpisode(this, item)
                    episodeName
                } else {
                    setTextColor(readColor(context, item.episode))
                    item.aeh.anime.title
                }
            }
        }
        if (binding.frontView.translationX == 0f) {
            binding.read.setImageResource(
                if (item.seen) R.drawable.ic_eye_off_24dp else R.drawable.ic_eye_24dp,
            )
        }

        binding.showMoreEpisodes.isVisible = item.aeh.extraEpisodes.isNotEmpty() &&
            !adapter.delegate.alwaysExpanded()
        binding.moreEpisodesLayout.isVisible = item.aeh.extraEpisodes.isNotEmpty() &&
            adapter.delegate.areExtraEpisodesExpanded(flexibleAdapterPosition)
        val moreVisible = binding.moreEpisodesLayout.isVisible

        binding.body.isVisible = !isSmallUpdates
        binding.body.text = when {
            item.aeh.episode.id == null -> context.timeSpanFromNow(MR.strings.added_, item.aeh.anime.date_added)
            isSmallUpdates -> ""
            item.aeh.history.id == null -> {
                if (isUpdates) {
                    if (adapter.sortByFetched) {
                        context.timeSpanFromNow(MR.strings.fetched_, item.episode.date_fetch)
                    } else {
                        context.timeSpanFromNow(MR.strings.updated_, item.episode.date_upload)
                    }
                } else {
                    context.timeSpanFromNow(MR.strings.fetched_, item.episode.date_fetch) + "\n" +
                        context.timeSpanFromNow(MR.strings.updated_, item.episode.date_upload)
                }
            }
            item.episode.id != item.aeh.episode.id -> readLastText(!moreVisible)
            // item.episode.last_second_seen > 0 && !item.episode.seen -> ... (Video progress)
            else -> context.timeSpanFromNow(MR.strings.read_, item.aeh.history.last_seen)
        }
        if ((context as? Activity)?.isDestroyed != true) {
             binding.coverThumbnail.load(item.aeh.anime.thumbnailUrl)
        }
        if (!item.aeh.anime.isLocal()) {
            notifyStatus(
                if (adapter.isSelected(flexibleAdapterPosition)) Download.State.CHECKED else item.status,
                item.progress,
                item.episode.seen,
            )
        }

        binding.showMoreEpisodes.setImageResource(
            if (moreVisible) {
                R.drawable.ic_expand_less_24dp
            } else {
                R.drawable.ic_expand_more_24dp
            },
        )
        val extraIds = binding.moreEpisodesLayout.children.toList().shorterList().map {
            it?.findViewById<DownloadButton>(R.id.download_button)?.tag
        }.toList()
        if (extraIds == item.aeh.extraEpisodes.shorterList().map { it?.id }) {
            var hasSameEpisode = false
            item.aeh.extraEpisodes.shorterList().forEachIndexed { index, episode ->
                val binding = 
                    RecentSubEpisodeItemBinding.bind(binding.moreEpisodesLayout.getChildAt(index))
                binding.configureView(episode, item)
                if (isUpdates && !binding.subtitle.text.isNullOrBlank() && !hasSameEpisode) {
                    showScanlatorInBody(moreVisible, item)
                    hasSameEpisode = true
                }
            }
            addMoreUpdatesText(!moreVisible, item)
        } else {
            binding.moreEpisodesLayout.removeAllViews()
            var hasSameEpisode = false
            if (item.aeh.extraEpisodes.isNotEmpty()) {
                item.aeh.extraEpisodes.shorterList().forEach { episode ->
                    val binding = RecentSubEpisodeItemBinding.inflate(
                        LayoutInflater.from(context), 
                        binding.moreEpisodesLayout,
                        true,
                    )
                    binding.configureView(episode, item)
                    if (isUpdates && !binding.subtitle.text.isNullOrBlank() && !hasSameEpisode) {
                        showScanlatorInBody(moreVisible, item)
                        hasSameEpisode = true
                    }
                }
                addMoreUpdatesText(!moreVisible, item)
            } else {
                episodeId = null
            }
        }
        listOf(binding.mainView, binding.downloadButton.root, binding.showMoreEpisodes, binding.cardLayout).forEach {
            it.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    binding.endView.translationY = binding.mainView.y
                    binding.endView.updateLayoutParams<ViewGroup.LayoutParams> {
                        height = binding.mainView.height
                    }
                    binding.read.setImageResource(
                        if (item.seen) R.drawable.ic_eye_off_24dp else R.drawable.ic_eye_24dp,
                    )
                    episodeId = null
                }
                false
            }
        }
    }

    private fun addMoreUpdatesText(add: Boolean, originalItem: RecentAnimeItem? = null) {
        val item = originalItem ?: adapter.getItem(bindingAdapterPosition) as? RecentAnimeItem ?: return
        val originalText = binding.body.text.toString()
        val andMoreText = itemView.context.getString(
            MR.plurals.notification_and_n_more,
            (item.aeh.extraEpisodes.size),
            (item.aeh.extraEpisodes.size),
        )
        if (add && item.aeh.extraEpisodes.isNotEmpty() && isUpdates &&
            !isSmallUpdates && !originalText.contains(andMoreText)
        ) {
            val text = "${originalText.substringBefore("\n")}\n$andMoreText"
            binding.body.text = text
        } else if (!add && originalText.contains(andMoreText)) {
            binding.body.text = originalText.removeSuffix("\n$andMoreText")
        }
    }

    private fun readLastText(show: Boolean, originalItem: RecentAnimeItem? = null): String {
        val item = originalItem ?: adapter.getItem(bindingAdapterPosition) as? RecentAnimeItem ?: return ""
        val notValidNum = item.aeh.episode.episode_number <= 0 // Simplified validation
        return if (item.episode.id != item.aeh.episode.id) {
            if (show) {
                itemView.context.timeSpanFromNow(MR.strings.read_, item.aeh.history.last_seen) + "\n"
            } else {
                ""
            } + itemView.context.getString(
                if (notValidNum) MR.strings.last_read_ else MR.strings.last_read_chapter_, // TODO: string resource for last watched
                if (notValidNum) item.aeh.episode.name else decimalFormat.format(item.aeh.episode.episode_number),
            )
        } else { "" }
    }

    private fun showScanlatorInBody(add: Boolean, originalItem: RecentAnimeItem? = null) {
        val item = originalItem ?: adapter.getItem(bindingAdapterPosition) as? RecentAnimeItem ?: return
        val originalText = binding.body.text.toString()
        binding.body.maxLines = 2
        val scanlator = item.episode.scanlator ?: return
        if (add) {
            if (isSmallUpdates) {
                binding.body.maxLines = 1
                binding.body.text = item.episode.scanlator
                binding.body.isVisible = true
            } else if (!originalText.contains(scanlator)) {
                val text = "${originalText.substringBefore("\n")}\n$scanlator"
                binding.body.text = text
            }
        } else {
            if (isSmallUpdates) {
                binding.body.isVisible = false
            } else {
                binding.body.text = originalText.removeSuffix("\n$scanlator")
                addMoreUpdatesText(true, item)
            }
        }
    }

    private fun <T> List<T>.shorterList(): List<T?> = 
        if (size > 21) take(10) + null + takeLast(10) else this

    @SuppressLint("ClickableViewAccessibility")
    private fun RecentSubEpisodeItemBinding.configureBlankView(count: Int) {
        val context = itemView.context
        title.text =
            context.getString(MR.plurals.notification_and_n_more, count, count)
        downloadButton.root.isVisible = false
        downloadButton.root.tag = null
        title.textSize = 13f
        title.setTextColor(context.contextCompatColor(R.color.read_chapter))
        textLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
            matchConstraintMinHeight = 16.dpToPx
        }
        root.tag = "sub ${-1L}"
        root.setOnLongClickListener { false }
        root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                episodeId = -1L
            }
            false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun RecentSubEpisodeItemBinding.configureView(episode: EpisodeHistory?, item: RecentAnimeItem) {
        if (episode?.id == null) {
            configureBlankView(item.aeh.extraEpisodes.size - 20)
            return
        }
        textLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
            matchConstraintMinHeight = 48.dpToPx
        }
        val context = itemView.context
        val showDLs = adapter.showDownloads
        title.text = episode.name // TODO: preferredEpisodeName
        // setTextViewForEpisode(title, episode)
        val notSeenYet = item.episode.id != item.aeh.episode.id && item.aeh.history.id != null
        subtitle.text = episode.history?.let { history -> 
            context.timeSpanFromNow(MR.strings.read_, history.last_seen)
                .takeIf { 
                    Date().time - history.last_seen < TimeUnit.DAYS.toMillis(1) || notSeenYet ||
                        adapter.dateFormat.run { 
                            format(history.last_seen) != format(item.aeh.history.last_seen)
                        }
                }
        } ?: ""
        // Simplified scanlator logic
        if (isUpdates && !episode.scanlator.isNullOrBlank()) {
             subtitle.text = episode.scanlator
        }

        subtitle.isVisible = subtitle.text.isNotBlank()
        title.textSize = (if (subtitle.isVisible) 14f else 14.5f)
        root.setOnClickListener {
            adapter.delegate.onSubEpisodeClicked(
                bindingAdapterPosition,
                episode,
                it,
            )
        }
        root.setOnLongClickListener {
            adapter.delegate.onItemLongClick(bindingAdapterPosition, episode)
        }
        listOf(root, downloadButton.root).forEach {
            it.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    binding.read.setImageResource(
                        if (episode.seen) R.drawable.ic_eye_off_24dp else R.drawable.ic_eye_24dp,
                    )
                    binding.endView.translationY = binding.moreEpisodesLayout.y + root.y
                    binding.endView.updateLayoutParams<ViewGroup.LayoutParams> {
                        height = root.height
                    }
                    episodeId = episode.id
                }
                false
            }
        }
        textLayout.updatePaddingRelative(start = if (isSmallUpdates) 64.dpToPx else 84.dpToPx)
        updateDivider()
        root.transitionName = "recents sub episode ${episode.id ?: 0L} transition"
        root.tag = "sub ${episode.id}"
        downloadButton.root.tag = episode.id
        val downloadInfo = 
            item.downloadInfo.find { it.chapterId == episode.id } ?: return
        downloadButton.downloadButton.setOnClickListener {
            downloadOrRemoveMenu(it, episode, downloadInfo.status)
        }
        downloadButton.downloadButton.isVisible = when (showDLs) {
            RecentAnimeAdapter.ShowRecentsDLs.None -> false
            RecentAnimeAdapter.ShowRecentsDLs.OnlyUnread, RecentAnimeAdapter.ShowRecentsDLs.UnreadOrDownloaded -> !episode.seen
            RecentAnimeAdapter.ShowRecentsDLs.OnlyDownloaded -> true
            RecentAnimeAdapter.ShowRecentsDLs.All -> true
        } && !item.aeh.anime.isLocal()
        notifySubStatus(
            episode,
            if (adapter.isSelected(flexibleAdapterPosition)) {
                Download.State.CHECKED
            } else {
                downloadInfo.status
            },
            downloadInfo.progress,
            episode.seen,
        )
    }

    private fun RecentSubEpisodeItemBinding.updateDivider() {
        divider.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            marginStart = if (isSmallUpdates) 64.dpToPx else 84.dpToPx
        }
    }

    override fun onLongClick(view: View?): Boolean {
        super.onLongClick(view)
        val item = adapter.getItem(flexibleAdapterPosition) as? RecentAnimeItem ?: return false
        return item.aeh.history.id != null
    }

    fun notifyStatus(status: Download.State, progress: Int, isSeen: Boolean, animated: Boolean = false) {
        binding.downloadButton.downloadButton.setDownloadStatus(status, progress, animated)
        val isEpisodeSeen = 
            if (adapter.showDownloads == RecentAnimeAdapter.ShowRecentsDLs.UnreadOrDownloaded) isSeen else true
        binding.downloadButton.downloadButton.isVisible = 
            when (adapter.showDownloads) {
                RecentAnimeAdapter.ShowRecentsDLs.UnreadOrDownloaded,
                RecentAnimeAdapter.ShowRecentsDLs.OnlyDownloaded,
                -> 
                    status !in Download.State.CHECKED..Download.State.NOT_DOWNLOADED || !isEpisodeSeen
                else -> binding.downloadButton.downloadButton.isVisible
            }
    }

    fun notifySubStatus(episode: Episode, status: Download.State, progress: Int, isSeen: Boolean, animated: Boolean = false) {
        val downloadButton = binding.moreEpisodesLayout.findViewWithTag<DownloadButton>(episode.id) ?: return
        downloadButton.setDownloadStatus(status, progress, animated)
        val isEpisodeSeen = 
            if (adapter.showDownloads == RecentAnimeAdapter.ShowRecentsDLs.UnreadOrDownloaded) isSeen else true
        downloadButton.isVisible = 
            when (adapter.showDownloads) {
                RecentAnimeAdapter.ShowRecentsDLs.UnreadOrDownloaded,
                RecentAnimeAdapter.ShowRecentsDLs.OnlyDownloaded,
                -> 
                    status !in Download.State.CHECKED..Download.State.NOT_DOWNLOADED || !isEpisodeSeen
                else -> downloadButton.isVisible
            }
    }

    override fun getFrontView(): View {
        return if (episodeId == null) { binding.mainView } else {
            binding.moreEpisodesLayout.children.find { it.tag == "sub $episodeId" }
                ?: binding.mainView
        }
    }

    override fun getRearEndView(): View? {
        return if (episodeId == -1L) null else binding.endView
    }

    private fun setTextViewForEpisode(textView: TextView, item: RecentAnimeItem) {
        val context = textView.context
        textView.setTextColor(readColor(context, item.episode))
        // setBookmark(textView, item.episode) // TODO
    }

    private fun readColor(context: Context, episode: Episode): Int {
        return if (episode.seen) context.contextCompatColor(R.color.read_chapter) else unreadColor(context)
    }

    private fun unreadColor(context: Context): Int {
        val color = context.getResourceColor(R.attr.colorOnSurface)
        return ColorUtils.setAlphaComponent(color, 255)
    }
}
