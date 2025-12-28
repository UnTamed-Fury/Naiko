package naiko.domain.extension.repo.interactor

import naiko.domain.extension.repo.ExtensionRepoRepository
import naiko.domain.extension.repo.model.ExtensionRepo

class ReplaceExtensionRepo(
    private val extensionRepoRepository: ExtensionRepoRepository
) {
    suspend fun await(repo: ExtensionRepo) {
        extensionRepoRepository.replaceRepository(repo)
    }
}
