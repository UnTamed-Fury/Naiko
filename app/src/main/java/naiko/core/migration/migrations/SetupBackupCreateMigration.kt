package naiko.core.migration.migrations

import android.app.Application
import eu.kanade.tachiyomi.data.backup.create.BackupCreatorJob
import naiko.core.migration.Migration
import naiko.core.migration.MigrationContext

class SetupBackupCreateMigration : Migration {
    override val version: Float = Migration.ALWAYS

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context = migrationContext.get<Application>() ?: return false
        BackupCreatorJob.setupTask(context)
        return true
    }
}
