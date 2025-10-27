package yokai.domain.extension.repo.interactor

import yokai.domain.extension.repo.ExtensionRepoRepository
import yokai.domain.extension.repo.model.ExtensionRepo

class ReplaceExtensionRepo(
    private val extensionRepoRepository: ExtensionRepoRepository
) {
    suspend fun await(repo: ExtensionRepo) {
        extensionRepoRepository.replaceRepository(repo)
    }
}
