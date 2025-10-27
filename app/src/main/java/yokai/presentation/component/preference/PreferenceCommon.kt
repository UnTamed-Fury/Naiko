package yokai.presentation.component.preference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.hippo.unifile.UniFile
import eu.kanade.tachiyomi.R
import yokai.i18n.MR
import yokai.util.lang.getString
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.core.storage.preference.collectAsState

@Composable
fun storageLocationText(
    storageDirPref: eu.kanade.tachiyomi.core.preference.Preference<String>,
): String {
    val context = LocalContext.current
    val storageDir by storageDirPref.collectAsState()

    if (storageDir == storageDirPref.defaultValue()) {
        return stringResource(MR.strings.no_location_set)
    }

    return remember(storageDir) {
        val file = UniFile.fromUri(context, storageDir.toUri())
        file?.filePath
    } ?: stringResource(MR.strings.invalid_location, storageDir)
}
