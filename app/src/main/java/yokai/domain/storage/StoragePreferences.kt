package yokai.domain.storage

import eu.kanade.tachiyomi.core.preference.Preference
import eu.kanade.tachiyomi.core.preference.PreferenceStore
import eu.kanade.tachiyomi.core.storage.FolderProvider

class StoragePreferences(
    private val folderProvider: FolderProvider,
    private val preferenceStore: PreferenceStore,
) {
    fun baseStorageDirectory() = preferenceStore.getString(Preference.appStateKey("storage_dir"), folderProvider.path())
}
