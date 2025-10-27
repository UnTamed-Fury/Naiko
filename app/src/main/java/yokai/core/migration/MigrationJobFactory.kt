package yokai.core.migration

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

class MigrationJobFactory(
    private val migrationContext: MigrationContext,
    private val scope: CoroutineScope
) {

    fun create(migrations: List<Migration>): Deferred<Boolean> = with(scope) {
        return migrations.sortedBy { it.version }
            .fold(CompletableDeferred(true)) { acc: Deferred<Boolean>, migration: Migration ->
                if (!migrationContext.dryRun) {
                    Logger.i { "Running migration: { name = ${migration::class.simpleName}, version = ${migration.version} }" }
                    async(start = CoroutineStart.UNDISPATCHED) {
                        val prev = acc.await()
                        migration(migrationContext) || prev
                    }
                } else {
                    Logger.i { "(Dry-run) Running migration: { name = ${migration::class.simpleName}, version = ${migration.version} }" }
                    CompletableDeferred(true)
                }
            }
    }
}
