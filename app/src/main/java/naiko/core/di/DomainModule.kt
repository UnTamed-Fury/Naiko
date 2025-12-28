package naiko.core.di

import org.koin.dsl.module
import naiko.data.category.CategoryRepositoryImpl
import naiko.data.chapter.ChapterRepositoryImpl
import naiko.data.extension.repo.ExtensionRepoRepositoryImpl
import naiko.data.history.HistoryRepositoryImpl
import naiko.data.library.custom.CustomMangaRepositoryImpl
import naiko.data.manga.MangaRepositoryImpl
import naiko.data.source.browse.filter.SavedSearchRepositoryImpl
import naiko.data.track.TrackRepositoryImpl
import naiko.domain.category.CategoryRepository
import naiko.domain.category.interactor.DeleteCategories
import naiko.domain.category.interactor.GetCategories
import naiko.domain.category.interactor.InsertCategories
import naiko.domain.category.interactor.SetMangaCategories
import naiko.domain.category.interactor.UpdateCategories
import naiko.domain.chapter.ChapterRepository
import naiko.domain.chapter.interactor.DeleteChapter
import naiko.domain.chapter.interactor.GetAvailableScanlators
import naiko.domain.chapter.interactor.GetChapter
import naiko.domain.chapter.interactor.InsertChapter
import naiko.domain.chapter.interactor.UpdateChapter
import naiko.domain.extension.interactor.TrustExtension
import naiko.domain.extension.repo.ExtensionRepoRepository
import naiko.domain.extension.repo.interactor.CreateExtensionRepo
import naiko.domain.extension.repo.interactor.DeleteExtensionRepo
import naiko.domain.extension.repo.interactor.GetExtensionRepo
import naiko.domain.extension.repo.interactor.GetExtensionRepoCount
import naiko.domain.extension.repo.interactor.ReplaceExtensionRepo
import naiko.domain.extension.repo.interactor.UpdateExtensionRepo
import naiko.domain.history.HistoryRepository
import naiko.domain.history.interactor.GetHistory
import naiko.domain.history.interactor.UpsertHistory
import naiko.domain.library.custom.CustomMangaRepository
import naiko.domain.library.custom.interactor.CreateCustomManga
import naiko.domain.library.custom.interactor.DeleteCustomManga
import naiko.domain.library.custom.interactor.GetCustomManga
import naiko.domain.library.custom.interactor.RelinkCustomManga
import naiko.domain.manga.MangaRepository
import naiko.domain.manga.interactor.GetLibraryManga
import naiko.domain.manga.interactor.GetManga
import naiko.domain.manga.interactor.InsertManga
import naiko.domain.manga.interactor.UpdateManga
import naiko.domain.recents.interactor.GetRecents
import naiko.domain.source.browse.filter.FilterSerializer
import naiko.domain.source.browse.filter.SavedSearchRepository
import naiko.domain.source.browse.filter.interactor.DeleteSavedSearch
import naiko.domain.source.browse.filter.interactor.GetSavedSearch
import naiko.domain.source.browse.filter.interactor.InsertSavedSearch
import naiko.domain.track.TrackRepository
import naiko.domain.track.interactor.DeleteTrack
import naiko.domain.track.interactor.GetTrack
import naiko.domain.track.interactor.InsertTrack

fun domainModule() = module {
    factory { TrustExtension(get(), get()) }

    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    factory { DeleteCategories(get()) }
    factory { GetCategories(get()) }
    factory { InsertCategories(get()) }
    factory { UpdateCategories(get()) }

    single<ExtensionRepoRepository> { ExtensionRepoRepositoryImpl(get()) }
    factory { CreateExtensionRepo(get()) }
    factory { DeleteExtensionRepo(get()) }
    factory { GetExtensionRepo(get()) }
    factory { GetExtensionRepoCount(get()) }
    factory { ReplaceExtensionRepo(get()) }
    factory { UpdateExtensionRepo(get(), get()) }

    single<CustomMangaRepository> { CustomMangaRepositoryImpl(get()) }
    factory { CreateCustomManga(get()) }
    factory { DeleteCustomManga(get()) }
    factory { GetCustomManga(get()) }
    factory { RelinkCustomManga(get()) }

    single<MangaRepository> { MangaRepositoryImpl(get()) }
    factory { GetManga(get()) }
    factory { GetLibraryManga(get()) }
    factory { InsertManga(get()) }
    factory { UpdateManga(get()) }

    factory { SetMangaCategories(get()) }

    single<ChapterRepository> { ChapterRepositoryImpl(get()) }
    factory { DeleteChapter(get()) }
    factory { GetAvailableScanlators(get()) }
    factory { GetChapter(get()) }
    factory { InsertChapter(get()) }
    factory { UpdateChapter(get()) }

    single<HistoryRepository> { HistoryRepositoryImpl(get()) }
    factory { GetHistory(get()) }
    factory { UpsertHistory(get()) }

    factory { GetRecents(get(), get()) }

    single<TrackRepository> { TrackRepositoryImpl(get()) }
    factory { DeleteTrack(get()) }
    factory { GetTrack(get()) }
    factory { InsertTrack(get()) }

    single<SavedSearchRepository> { SavedSearchRepositoryImpl(get()) }
    factory { DeleteSavedSearch(get()) }
    factory { GetSavedSearch(get()) }
    factory { InsertSavedSearch(get()) }
    factory { FilterSerializer() }
}
