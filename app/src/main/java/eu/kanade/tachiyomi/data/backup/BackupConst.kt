package eu.kanade.tachiyomi.data.backup

import eu.kanade.tachiyomi.BuildConfig.APPLICATION_ID as ID

object BackupConst {
    private const val NAME = "BackupRestorer"
    const val EXTRA_URI = "$ID.$NAME.EXTRA_URI"
}
