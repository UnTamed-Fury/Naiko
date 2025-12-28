package naiko.domain.category.interactor

import eu.kanade.tachiyomi.data.database.models.Category
import naiko.domain.category.CategoryRepository
import naiko.domain.category.models.CategoryUpdate

class DeleteCategories(
    private val categoryRepository: CategoryRepository,
) {
//    suspend fun await(updates: List<Int>) =
    suspend fun awaitOne(id: Long) = categoryRepository.delete(id)
}
