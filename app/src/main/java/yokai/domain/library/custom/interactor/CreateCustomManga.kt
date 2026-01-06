package yokai.domain.library.custom.interactor

import yokai.domain.library.custom.CustomMangaRepository
import yokai.domain.library.custom.exception.SaveCustomMangaException
import yokai.domain.library.custom.model.CustomMangaInfo

class CreateCustomManga(
    private val customMangaRepository: CustomMangaRepository,
) {
    suspend fun await(mangaInfo: CustomMangaInfo): Result {
        try {
            customMangaRepository.insertCustomManga(mangaInfo)
            return Result.Success
        } catch (exc: SaveCustomMangaException) {
            return Result.Error
        }
    }

    suspend fun bulk(mangaList: List<CustomMangaInfo>): Result {
        try {
            customMangaRepository.insertBulkCustomManga(mangaList)
            return Result.Success
        } catch (exc: SaveCustomMangaException) {
            return Result.Error
        }
    }

    sealed interface Result {
        data object Success : Result
        data object Error : Result
    }
}
