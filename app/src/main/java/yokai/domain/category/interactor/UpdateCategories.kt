package yokai.domain.category.interactor

import eu.kanade.tachiyomi.data.database.models.Category
import yokai.domain.category.CategoryRepository
import yokai.domain.category.models.CategoryUpdate

class UpdateCategories(
    private val categoryRepository: CategoryRepository,
) {
    suspend fun await(updates: List<CategoryUpdate>) = categoryRepository.updateAll(updates)
    suspend fun awaitOne(update: CategoryUpdate) = categoryRepository.update(update)
}
