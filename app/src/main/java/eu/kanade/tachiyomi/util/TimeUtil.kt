package eu.kanade.tachiyomi.util

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import eu.kanade.tachiyomi.R
import yokai.i18n.MR
import yokai.util.lang.getString
import dev.icerock.moko.resources.compose.stringResource
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

@Composable
fun relativeTimeSpanString(epochMillis: Long): String {
    val now = Instant.now().toEpochMilli()
    return when {
        epochMillis <= 0L -> stringResource(MR.strings.never)
        now - epochMillis < 1.minutes.inWholeMilliseconds -> stringResource(
            MR.strings.just_now,
        )
        else -> DateUtils.getRelativeTimeSpanString(epochMillis, now, DateUtils.MINUTE_IN_MILLIS).toString()
    }
}
