package eu.kanade.tachiyomi.core.storage

import android.content.Context
import android.os.Environment
import androidx.core.net.toUri
import yokai.i18n.MR
import yokai.util.lang.getString
import java.io.File

class AndroidStorageFolderProvider(
    private val context: Context,
) : FolderProvider {

    override fun directory(): File {
        return File(
            Environment.getExternalStorageDirectory().absolutePath + File.separator +
                context.getString(MR.strings.app_normalized_name),
        )
    }

    override fun path(): String {
        return directory().toUri().toString()
    }
}
