package eu.kanade.tachiyomi.util.lang

import java.text.DateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

fun LocalDateTime.toDateTimestampString(dateTimeFormatter: DateTimeFormatter): String {
    val date = dateTimeFormatter.format(this)
    val time = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(this)
    return "$date $time"
}

fun Date.toTimestampString(dateFormatter: DateFormat): String {
    val date = dateFormatter.format(this)
    val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(this)
    return "$date $time"
}
