package yokai.core.migration.migrations

import android.app.Application
import eu.kanade.tachiyomi.extension.ExtensionUpdateJob
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext

class SetupExtensionUpdateMigration : Migration {
    override val version: Float = Migration.ALWAYS

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context = migrationContext.get<Application>() ?: return false
        ExtensionUpdateJob.setupTask(context)
        return true
    }
}
