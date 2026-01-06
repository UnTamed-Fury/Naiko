package yokai.core.migration.migrations

import android.app.Application
import androidx.preference.PreferenceManager
import yokai.core.migration.Migration
import yokai.core.migration.MigrationContext
import yokai.domain.base.BasePreferences

/**
 * Upstream no longer use Int for extension installer prefs, this solves incompatibility with upstreams backup
 */
class ExtensionInstallerEnumMigration : Migration {
    override val version: Float = 119f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val basePreferences = migrationContext.get<BasePreferences>() ?: return false
        val context = migrationContext.get<Application>() ?: return false
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        try {
            val oldExtensionInstall = prefs.getInt("extension_installer", 0)
            basePreferences.extensionInstaller().set(BasePreferences.ExtensionInstaller.migrate(oldExtensionInstall))
        } catch (_: Exception) {
        }
        return true
    }
}
