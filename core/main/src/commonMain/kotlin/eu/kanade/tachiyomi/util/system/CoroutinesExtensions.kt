package eu.kanade.tachiyomi.util.system

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(DelicateCoroutinesApi::class)
fun launchUI(block: suspend CoroutineScope.() -> Unit): Job =
    GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT, block)

@OptIn(DelicateCoroutinesApi::class)
fun launchIO(block: suspend CoroutineScope.() -> Unit): Job =
    GlobalScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT, block)

@OptIn(DelicateCoroutinesApi::class)
fun launchNow(block: suspend CoroutineScope.() -> Unit): Job =
    GlobalScope.launch(Dispatchers.Main, CoroutineStart.UNDISPATCHED, block)

fun CoroutineScope.launchIO(block: suspend CoroutineScope.() -> Unit): Job =
    launch(Dispatchers.IO, block = block)

fun CoroutineScope.launchUI(block: suspend CoroutineScope.() -> Unit): Job =
    launch(Dispatchers.Main, block = block)

fun CoroutineScope.launchNonCancellableIO(block: suspend CoroutineScope.() -> Unit): Job =
    launchIO { withContext(NonCancellable, block) }

suspend fun <T> withUIContext(block: suspend CoroutineScope.() -> T) = withContext(Dispatchers.Main, block)

suspend fun <T> withIOContext(block: suspend CoroutineScope.() -> T) = withContext(Dispatchers.IO, block)

suspend fun <T> withDefContext(block: suspend CoroutineScope.() -> T) = withContext(Dispatchers.Default, block)
