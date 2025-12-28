package naiko.core.migration.migrations

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import naiko.core.migration.Migration

val migrations: ImmutableList<Migration> = persistentListOf(
    // Always run
    SetupAppUpdateMigration(),
    SetupBackupCreateMigration(),
    SetupExtensionUpdateMigration(),
    SetupLibraryUpdateMigration(),

    // Naiko fork
    CutoutMigration(),
    ExtensionInstallerEnumMigration(),
    RepoJsonMigration(),
    ThePurgeMigration(),
)
