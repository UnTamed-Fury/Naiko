package yokai.core.archive.util

import android.content.Context
import android.os.ParcelFileDescriptor
import com.hippo.unifile.UniFile
import yokai.core.archive.ArchiveReader
import yokai.core.archive.EpubReader

fun UniFile.openFileDescriptor(context: Context, mode: String): ParcelFileDescriptor =
    context.contentResolver.openFileDescriptor(uri, mode) ?: error("Failed to open file descriptor: ${filePath ?: uri.toString()}")

fun UniFile.archiveReader(context: Context): ArchiveReader = openFileDescriptor(context, "r").use { ArchiveReader(it) }

fun UniFile.epubReader(context: Context): EpubReader = EpubReader(archiveReader(context))
