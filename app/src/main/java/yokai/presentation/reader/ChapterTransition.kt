package yokai.presentation.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.icerock.moko.resources.compose.pluralStringResource
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.data.database.models.Chapter
import eu.kanade.tachiyomi.domain.manga.models.Manga
import eu.kanade.tachiyomi.ui.reader.model.ChapterTransition
import eu.kanade.tachiyomi.ui.reader.viewer.missingChapterCount
import eu.kanade.tachiyomi.util.chapter.ChapterUtil.Companion.preferredChapterName
import kotlinx.collections.immutable.persistentMapOf
import yokai.i18n.MR
import yokai.util.secondaryItemAlpha

@Composable
fun ChapterTransition(
    manga: Manga,
    transition: ChapterTransition,
    currChapterDownloaded: Boolean,
    goingToChapterDownloaded: Boolean,
) {
    val currChapter = transition.from.chapter
    val goingToChapter = transition.to?.chapter
    val chapterGap = missingChapterCount(transition)

    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
        when (transition) {
            is ChapterTransition.Prev -> {
                TransitionText(
                    manga = manga,
                    topLabel = stringResource(MR.strings.previous_title),
                    topChapter = goingToChapter,
                    topChapterDownloaded = goingToChapterDownloaded,
                    bottomLabel = stringResource(MR.strings.current_chapter),
                    bottomChapter = currChapter,
                    bottomChapterDownloaded = currChapterDownloaded,
                    fallbackLabel = stringResource(MR.strings.theres_no_previous_chapter),
                    chapterGap = chapterGap,
                )
            }
            is ChapterTransition.Next -> {
                TransitionText(
                    manga = manga,
                    topLabel = stringResource(MR.strings.finished_chapter),
                    topChapter = currChapter,
                    topChapterDownloaded = currChapterDownloaded,
                    bottomLabel = stringResource(MR.strings.next_title),
                    bottomChapter = goingToChapter,
                    bottomChapterDownloaded = goingToChapterDownloaded,
                    fallbackLabel = stringResource(MR.strings.theres_no_next_chapter),
                    chapterGap = chapterGap,
                )
            }
        }
    }
}

@Composable
private fun TransitionText(
    manga: Manga,
    topLabel: String,
    topChapter: Chapter?,
    topChapterDownloaded: Boolean,
    bottomLabel: String,
    bottomChapter: Chapter?,
    bottomChapterDownloaded: Boolean,
    fallbackLabel: String,
    chapterGap: Int,
) {
    Column (
        modifier = Modifier
            .widthIn(max = 460.dp)
            .fillMaxWidth(),
    ) {
        if (topChapter != null) {
            ChapterText(
                header = topLabel,
                name = topChapter.preferredChapterName(manga),
                scanlator = topChapter.scanlator,
                otherDownloaded = bottomChapterDownloaded,
                downloaded = topChapterDownloaded,
            )

            Spacer(Modifier.height(VerticalSpacerSize))
        } else {
            NoChapterNotification(
                text = fallbackLabel,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }

        if (bottomChapter != null) {
            if (chapterGap > 0) {
                ChapterGapWarning(
                    gapCount = chapterGap,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            Spacer(Modifier.height(VerticalSpacerSize))

            ChapterText(
                header = bottomLabel,
                name = bottomChapter.preferredChapterName(manga),
                scanlator = bottomChapter.scanlator,
                otherDownloaded = topChapterDownloaded,
                downloaded = bottomChapterDownloaded,
            )
        } else {
            NoChapterNotification(
                text = fallbackLabel,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
private fun NoChapterNotification(
    text: String,
    modifier: Modifier = Modifier,
) {
    OutlinedCard (
        modifier = modifier,
        colors = CardColor,
    ) {
        Row (
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null,
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ChapterGapWarning(
    gapCount: Int,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier,
        colors = CardColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Warning,
                tint = MaterialTheme.colorScheme.error,
                contentDescription = null,
            )

            Text(
                text = pluralStringResource(MR.plurals.missing_chapters_warning, quantity = gapCount, gapCount),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ChapterHeaderText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
private fun ChapterText(
    header: String,
    name: String,
    scanlator: String?,
    otherDownloaded: Boolean,
    downloaded: Boolean,
) {
    Column {
        ChapterHeaderText(
            text = header,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        Text(
            text = buildAnnotatedString {
                if (downloaded || otherDownloaded) {
                    if (downloaded) {
                        appendInlineContent(DOWNLOADED_ICON_ID)
                    } else {
                        appendInlineContent(ONLINE_ICON_ID)
                    }
                    append(' ')
                }
                append(name)
            },
            fontSize = 20.sp,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleLarge,
            inlineContent = persistentMapOf(
                DOWNLOADED_ICON_ID to InlineTextContent(
                    Placeholder(
                        width = 22.sp,
                        height = 22.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = stringResource(MR.strings.downloaded),
                    )
                },
                ONLINE_ICON_ID to InlineTextContent(
                    Placeholder(
                        width = 22.sp,
                        height = 22.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Cloud,
                        contentDescription = stringResource(MR.strings.not_downloaded),
                    )
                },
            ),
        )

        scanlator?.let {
            Text(
                text = it,
                modifier = Modifier
                    .secondaryItemAlpha()
                    .padding(top = 2.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private val CardColor: CardColors
    @Composable
    get() = CardDefaults.outlinedCardColors(
        containerColor = Color.Transparent,
        contentColor = LocalContentColor.current,
    )

private val VerticalSpacerSize = 24.dp
private const val DOWNLOADED_ICON_ID = "downloaded"
private const val ONLINE_ICON_ID = "online"
