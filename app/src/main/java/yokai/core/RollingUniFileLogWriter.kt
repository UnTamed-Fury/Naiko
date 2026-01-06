package yokai.core

import co.touchlab.kermit.DefaultFormatter
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Message
import co.touchlab.kermit.MessageStringFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Tag
import com.hippo.unifile.UniFile
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.util.system.launchIO
import eu.kanade.tachiyomi.util.system.withIOContext
import java.io.IOException
import java.io.OutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.newSingleThreadContext

/**
 * Copyright (c) 2024 Touchlab
 * SPDX-License-Identifier: Apache-2.0
 *
 * Kermit's RollingFileLogWriter, modified to use UniFile since using KotlinX IO's FileSystem keep throwing
 * "Permission Denied". Also added try-catch, in case "Permission Denied" is back to haunt me
 *
 * REF: https://github.com/touchlab/Kermit/blob/c9af0b7d3344b430f4ed2668e74d02f34ba1905a/kermit-io/src/commonMain/kotlin/co/touchlab/kermit/io/RollingFileLogWriter.kt
 */
class RollingUniFileLogWriter(
    private val logPath: UniFile,
    private val rollOnSize: Long = 10 * 1024 * 1024, // 10MB
    private val maxRolledLogFiles: Int = 5,
    private val maxLogFiles: Int = 5,
    private val messageStringFormatter: MessageStringFormatter = DefaultFormatter,
    private val messageDateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()),
    private val isVerbose: Boolean = false,
) : LogWriter() {
    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    private val coroutineScope = CoroutineScope(
        newSingleThreadContext("RollingUniFileLogWriter") +
            SupervisorJob() +
            CoroutineName("RollingUniFileLogWriter") +
            CoroutineExceptionHandler { _, throwable ->
                println("RollingUniFileLogWriter: Uncaught exception in writer coroutine")
                throwable.printStackTrace()
            }
    )

    private val loggingChannel: Channel<ByteArray> = Channel()

    init {
        coroutineScope.launchIO {
            writer()
        }
    }

    override fun isLoggable(tag: String, severity: Severity): Boolean =
        severity >= if (isVerbose) Severity.Debug else Severity.Info

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        bufferLog(
            formatMessage(
                severity = severity,
                tag = Tag(tag),
                message = Message(message),
            ), throwable
        )
    }

    private fun bufferLog(message: String, throwable: Throwable?) {
        val log = buildString {
            append(messageDateFormat.format(Date()))
            append(" ")
            appendLine(message)
            if (throwable != null) {
                appendLine(throwable.stackTraceToString())
            }
        }
        loggingChannel.trySendBlocking(log.toByteArray())
    }

    private fun formatMessage(severity: Severity, tag: Tag?, message: Message): String =
        messageStringFormatter.formatMessage(severity, tag, message)

    private fun maybeRollLogs(size: Long): Boolean {
        return if (size > rollOnSize) {
            rollLogs()
            true
        } else false
    }

    private fun rollLogs() {
        if (pathForLogIndex(maxRolledLogFiles - 1)?.exists() == true) {
            pathForLogIndex(maxRolledLogFiles - 1)?.delete()
        }

        (0..<(maxRolledLogFiles - 1)).reversed().forEach {
            val sourcePath = pathForLogIndex(it)
            val targetFileName = fileNameForLogIndex(it + 1)
            if (sourcePath?.exists() == true) {
                try {
                    sourcePath.renameTo(targetFileName)
                } catch (e: Exception) {
                    println("RollingUniFileLogWriter: Failed to roll log file ${sourcePath.filePath} to $targetFileName (sourcePath exists=${sourcePath.exists()})")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun fileNameForLogIndex(index: Int): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val name = "${date}-${BuildConfig.BUILD_TYPE}"
        return if (index == 0) "${name}.log" else "$name (${index}).log"
    }

    private fun pathForLogIndex(index: Int, create: Boolean = false): UniFile? {
        return if (create) logPath.createFile(fileNameForLogIndex(index)) else logPath.findFile(fileNameForLogIndex(index))
    }

    private suspend fun writer() = withIOContext {
        val logFilePath = pathForLogIndex(0)

        if (logFilePath?.exists() == true) {
            maybeRollLogs(fileSize(logFilePath))
        }

        fun openNewOutput(): OutputStream? {
            val newLog = pathForLogIndex(0, true)
            val dupes = mutableMapOf<String, List<UniFile>>()
            logPath
                .listFiles { file, filename ->
                    val match = LOG_FILE_REGEX.find(filename)
                    match?.groupValues?.get(1)?.let { key ->
                        dupes["${key}.log"] = dupes["${key}.log"].orEmpty() + listOf(file)
                    }

                    match == null
                }
                .orEmpty()
                .sortedByDescending { it.name }
                .drop(maxLogFiles)
                .forEach {
                    it.delete()
                    dupes[it.name]?.forEach { f -> f.delete() }
                }
            return newLog?.openOutputStream(true)
        }

        var currentLogSink = openNewOutput()

        while (currentCoroutineContext().isActive) {
            val result = loggingChannel.receiveCatching()

            val rolled = maybeRollLogs(fileSize(logFilePath))
            if (rolled) {
                currentLogSink?.close()
                currentLogSink = openNewOutput()
            }

            result.getOrNull()?.let {
                try {
                    currentLogSink?.write(it)
                } catch (e: IOException) {
                    // Probably "Permission Denied" is back to haunt me
                    println("RollingUniFileLogWriter: Failed to write to log file")
                    e.printStackTrace()
                }
            }

            currentLogSink?.flush()
        }
    }

    private fun fileSize(path: UniFile?) = path?.length() ?: -1L

    companion object {
        private val LOG_FILE_REGEX = """(\d+-\d+-\d+-${BuildConfig.BUILD_TYPE}) \(\d+\)\.log""".toRegex()
    }
}
