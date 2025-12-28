package naiko.domain.chapter.interactor

import naiko.domain.chapter.ChapterRepository
import naiko.domain.chapter.models.ChapterUpdate

class UpdateChapter(
    private val chapterRepository: ChapterRepository,
) {
    suspend fun await(chapter: ChapterUpdate) = chapterRepository.update(chapter)
    suspend fun awaitAll(chapters: List<ChapterUpdate>) = chapterRepository.updateAll(chapters)
}
