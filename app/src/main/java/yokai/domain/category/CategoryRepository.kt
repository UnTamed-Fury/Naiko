package yokai.domain.category

import eu.kanade.tachiyomi.data.database.models.Category
import kotlinx.coroutines.flow.Flow
import yokai.domain.category.models.CategoryUpdate

interface CategoryRepository {
    suspend fun getAll(): List<Category>
    suspend fun getAllByMangaId(mangaId: Long): List<Category>
    fun getAllAsFlow(): Flow<List<Category>>
    suspend fun insert(category: Category): Long?
    suspend fun insertBulk(categories: List<Category>)
    suspend fun update(update: CategoryUpdate): Boolean
    suspend fun updateAll(updates: List<CategoryUpdate>): Boolean
    suspend fun delete(id: Long)
}
