package yokai.presentation.settings.screen.data

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.hippo.unifile.UniFile
import eu.kanade.tachiyomi.core.preference.Preference
import eu.kanade.tachiyomi.util.system.tryTakePersistableUriPermission

@Composable
fun storageLocationPicker(
    baseStorageDirectory: Preference<String>,
): ManagedActivityResultLauncher<Uri?, Uri?> {
    val context = LocalContext.current

    return rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION

            context.tryTakePersistableUriPermission(uri, flags)
            UniFile.fromUri(context, uri)?.let {
                baseStorageDirectory.set(it.uri.toString())
            }
        }
    }
}
