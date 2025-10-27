package yokai.core.migration.migrations

import android.app.Application
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext

class ThePurgeMigration : Migration {
    override val version: Float = 112f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context = migrationContext.get<Application>() ?: return false
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        prefs.edit {
            remove("trusted_signatures")
        }
        return true
    }
}
