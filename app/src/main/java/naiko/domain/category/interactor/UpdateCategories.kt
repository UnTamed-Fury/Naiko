package naiko.domain.category.interactor

import eu.kanade.tachiyomi.data.database.models.Category
import naiko.domain.category.CategoryRepository
import naiko.domain.category.models.CategoryUpdate

class UpdateCategories(
    private val categoryRepository: CategoryRepository,
) {
    suspend fun await(updates: List<CategoryUpdate>) = categoryRepository.updateAll(updates)
    suspend fun awaitOne(update: CategoryUpdate) = categoryRepository.update(update)
}
