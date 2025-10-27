package eu.kanade.tachiyomi.core.storage.preference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import eu.kanade.tachiyomi.core.preference.Preference
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun <T> Preference<T>.collectAsState(): State<T> {
    val flow = remember(this) { changes() }
    return flow.collectAsState(initial = get())
}

fun String.asDateFormat(): DateFormat = when (this) {
    "" -> DateFormat.getDateInstance(DateFormat.SHORT)
    else -> SimpleDateFormat(this, Locale.getDefault())
}
