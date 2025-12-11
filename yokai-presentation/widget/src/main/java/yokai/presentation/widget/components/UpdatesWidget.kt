package yokai.presentation.widget.components

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text
import yokai.i18n.MR
import yokai.presentation.core.Constants
import yokai.presentation.core.util.IntentCommon
import yokai.presentation.widget.ContainerModifier
import yokai.presentation.widget.util.calculateRowAndColumnCount
import yokai.presentation.widget.util.stringResource

@Composable
fun UpdatesWidget(data: List<Pair<Long, Bitmap?>>?) {
    val (rowCount, columnCount) = LocalSize.current.calculateRowAndColumnCount()
    val clazz = Class.forName(Constants.MAIN_ACTIVITY)
    val mainIntent = Intent(LocalContext.current, clazz).setAction(Constants.SHORTCUT_RECENTS)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    Column(
        modifier = ContainerModifier.clickable(actionStartActivity(mainIntent)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (data == null) {
            CircularProgressIndicator()
        } else if (data.isEmpty()) {
            Text(text = stringResource(MR.strings.no_recent_read_updated_manga))
        } else {
            (0 until rowCount).forEach { i ->
                val coverRow = (0 until columnCount).mapNotNull { j ->
                    data.getOrNull(j + (i * columnCount))
                }
                if (coverRow.isNotEmpty()) {
                    Row(
                        modifier = GlanceModifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        coverRow.forEach { (mangaId, cover) ->
                            Box(
                                modifier = GlanceModifier
                                    .padding(horizontal = 3.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                val intent = IntentCommon.openManga(LocalContext.current, mangaId, true)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    // https://issuetracker.google.com/issues/238793260
                                    .addCategory(mangaId.toString())
                                UpdatesMangaCover(
                                    modifier = GlanceModifier.clickable(actionStartActivity(intent)),
                                    cover = cover,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
