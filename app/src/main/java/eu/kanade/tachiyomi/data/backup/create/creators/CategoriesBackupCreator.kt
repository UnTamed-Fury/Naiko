package eu.kanade.tachiyomi.data.backup.create.creators

import eu.kanade.tachiyomi.data.backup.create.BackupOptions
import eu.kanade.tachiyomi.data.backup.models.BackupCategory
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import yokai.domain.category.interactor.GetCategories

class CategoriesBackupCreator(
    private val getCategories: GetCategories = Injekt.get(),
) {
    /**
     * Backup the categories of library
     *
     * @return list of [BackupCategory] to be backed up
     */
    suspend operator fun invoke(): List<BackupCategory> {
        return getCategories.await()
            .map { BackupCategory.copyFrom(it) }
    }
}
