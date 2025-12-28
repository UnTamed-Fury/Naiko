package naiko.core.migration.migrations

import co.touchlab.kermit.Logger
import eu.kanade.tachiyomi.core.preference.Preference
import eu.kanade.tachiyomi.core.preference.PreferenceStore
import eu.kanade.tachiyomi.util.system.withIOContext
import naiko.core.migration.Migration
import naiko.core.migration.MigrationContext
import naiko.domain.extension.repo.ExtensionRepoRepository
import naiko.domain.extension.repo.exception.SaveExtensionRepoException

class RepoJsonMigration : Migration {
    override val version: Float = 130f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean = withIOContext {
        val extensionRepoRepository: ExtensionRepoRepository = migrationContext.get() ?: return@withIOContext false
        val preferenceStore: PreferenceStore = migrationContext.get() ?: return@withIOContext false
        val extensionRepos: Preference<Set<String>> = preferenceStore.getStringSet("extension_repos", emptySet())

        for ((index, source) in extensionRepos.get().withIndex()) {
            try {
                extensionRepoRepository.upsertRepository(
                    source,
                    "Repo #${index + 1}",
                    null,
                    source,
                    "NOFINGERPRINT-${index + 1}",
                )
            } catch (e: SaveExtensionRepoException) {
                Logger.e(e) { "Error Migrating Extension Repo with baseUrl: $source" }
            }
        }
        extensionRepos.delete()

        return@withIOContext true
    }
}
