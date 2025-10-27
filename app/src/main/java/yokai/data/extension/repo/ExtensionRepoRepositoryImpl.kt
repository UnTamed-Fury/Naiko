package yokai.data.extension.repo

import android.database.sqlite.SQLiteException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import yokai.data.DatabaseHandler
import yokai.domain.extension.repo.ExtensionRepoRepository
import yokai.domain.extension.repo.exception.SaveExtensionRepoException
import yokai.domain.extension.repo.model.ExtensionRepo

class ExtensionRepoRepositoryImpl(private val handler: DatabaseHandler): ExtensionRepoRepository {
    override fun subscribeAll(): Flow<List<ExtensionRepo>> =
        handler.subscribeToList { extension_reposQueries.findAll(::mapExtensionRepo) }

    override suspend fun getAll(): List<ExtensionRepo> =
        handler.awaitList { extension_reposQueries.findAll(::mapExtensionRepo) }

    override suspend fun getRepository(baseUrl: String): ExtensionRepo? =
        handler.awaitOneOrNull { extension_reposQueries.findOne(baseUrl, ::mapExtensionRepo) }

    override suspend fun getRepositoryBySigningKeyFingerprint(fingerprint: String): ExtensionRepo? =
        handler.awaitOneOrNull { extension_reposQueries.findOneBySigningKeyFingerprint(fingerprint, ::mapExtensionRepo) }

    override fun getCount(): Flow<Int> =
        handler.subscribeToOne { extension_reposQueries.count() }.map { it.toInt() }

    override suspend fun insertRepository(
        baseUrl: String,
        name: String,
        shortName: String?,
        website: String,
        signingKeyFingerprint: String
    ) {
        try {
            handler.await { extension_reposQueries.insert(baseUrl, name, shortName, website, signingKeyFingerprint) }
        } catch (exc: SQLiteException) {
            throw SaveExtensionRepoException(exc)
        }
    }

    override suspend fun upsertRepository(
        baseUrl: String,
        name: String,
        shortName: String?,
        website: String,
        signingKeyFingerprint: String
    ) {
        try {
            handler.await { extension_reposQueries.upsert(baseUrl, name, shortName, website, signingKeyFingerprint) }
        } catch (exc: SQLiteException) {
            throw SaveExtensionRepoException(exc)
        }
    }

    override suspend fun replaceRepository(newRepo: ExtensionRepo) {
        handler.await {
            extension_reposQueries.replace(
                newRepo.baseUrl,
                newRepo.name,
                newRepo.shortName,
                newRepo.website,
                newRepo.signingKeyFingerprint,
            )
        }
    }

    override suspend fun deleteRepository(baseUrl: String) {
        handler.await { extension_reposQueries.delete(baseUrl) }
    }

    private fun mapExtensionRepo(
        baseUrl: String,
        name: String,
        shortName: String?,
        website: String,
        signingKeyFingerprint: String,
    ): ExtensionRepo = ExtensionRepo(baseUrl, name, shortName, website, signingKeyFingerprint)
}
