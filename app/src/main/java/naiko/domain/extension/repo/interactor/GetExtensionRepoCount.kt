package naiko.domain.extension.repo.interactor

import naiko.domain.extension.repo.ExtensionRepoRepository

class GetExtensionRepoCount(
    private val extensionRepoRepository: ExtensionRepoRepository
) {
    fun subscribe() = extensionRepoRepository.getCount()
}
