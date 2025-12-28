package naiko.domain.library.custom.interactor

import naiko.domain.library.custom.CustomMangaRepository

class GetCustomManga(
    private val customMangaRepository: CustomMangaRepository,
) {
    fun subscribeAll() = customMangaRepository.subscribeAll()

    suspend fun getAll() = customMangaRepository.getAll()
}
