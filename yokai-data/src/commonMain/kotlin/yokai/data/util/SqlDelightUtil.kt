package yokai.data.util

import app.cash.sqldelight.ExecutableQuery
import app.cash.sqldelight.Query
import app.cash.sqldelight.db.QueryResult
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

fun <T : Any> ExecutableQuery<T>.executeAsFirst(): T {
    return executeAsFirstOrNull() ?: throw NullPointerException("ResultSet returned null for $this")
}

fun <T : Any> ExecutableQuery<T>.executeAsFirstOrNull(): T? = execute { cursor ->
    if (!cursor.next().value) return@execute QueryResult.Value(null)
    QueryResult.Value(mapper(cursor))
}.value

suspend fun <T : Any> ExecutableQuery<T>.awaitAsFirst(): T {
    return awaitAsFirstOrNull()
        ?: throw NullPointerException("ResultSet returned null for $this")
}

suspend fun <T : Any> ExecutableQuery<T>.awaitAsFirstOrNull(): T? = execute { cursor ->
    // If the cursor isn't async, we want to preserve the blocking semantics and execute it synchronously
    when (val next = cursor.next()) {
        is QueryResult.AsyncValue -> {
            QueryResult.AsyncValue {
                if (!next.await()) return@AsyncValue null
                mapper(cursor)
            }
        }

        is QueryResult.Value -> {
            if (!next.value) return@execute QueryResult.Value(null)
            QueryResult.Value(mapper(cursor))
        }
    }
}.await()

fun <T : Any> Flow<Query<T>>.mapToFirstOrNull(
    context: CoroutineContext,
): Flow<T?> = map {
    withContext(context) {
        it.awaitAsFirstOrNull()
    }
}
