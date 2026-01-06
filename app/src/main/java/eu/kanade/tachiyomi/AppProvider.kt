package eu.kanade.tachiyomi

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import eu.kanade.tachiyomi.ui.crash.CrashActivity
import eu.kanade.tachiyomi.ui.crash.GlobalExceptionHandler

class AppProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        context?.let {
            GlobalExceptionHandler.initialize(it, CrashActivity::class.java)
            return true
        }
        return false
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
