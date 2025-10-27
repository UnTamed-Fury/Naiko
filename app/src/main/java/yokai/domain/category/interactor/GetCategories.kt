package yokai.domain.category.interactor

import yokai.domain.category.CategoryRepository

class GetCategories(
    private val categoryRepository: CategoryRepository,
) {
    suspend fun await() = categoryRepository.getAll()
    suspend fun awaitByMangaId(mangaId: Long?) = mangaId?.let { categoryRepository.getAllByMangaId(it) }.orEmpty()
    fun subscribe() = categoryRepository.getAllAsFlow()
}
