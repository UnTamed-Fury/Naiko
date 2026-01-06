package yokai.domain.extension.interactor

import android.content.pm.PackageInfo
import androidx.core.content.pm.PackageInfoCompat
import eu.kanade.tachiyomi.core.preference.getAndSet
import yokai.domain.extension.repo.ExtensionRepoRepository
import yokai.domain.source.SourcePreferences

class TrustExtension(
    private val extensionRepoRepository: ExtensionRepoRepository,
    private val sourcePreferences: SourcePreferences,
) {
    suspend fun isTrustedByRepo(fingerprints: List<String>): Boolean {
        val trustedFingerprints = extensionRepoRepository.getAll().map { it.signingKeyFingerprint }.toHashSet()
        return trustedFingerprints.any { fingerprints.contains(it) }
    }

    suspend fun isTrusted(pkgInfo: PackageInfo, fingerprints: List<String>): Boolean {
        val key = "${pkgInfo.packageName}:${PackageInfoCompat.getLongVersionCode(pkgInfo)}:${fingerprints.last()}"
        return isTrustedByRepo(fingerprints) || key in sourcePreferences.trustedExtensions().get()
    }

    fun trust(pkgName: String, versionCode: Long, signatureHash: String) {
       sourcePreferences.trustedExtensions().getAndSet { exts ->
           val removed = exts.filterNot { it.startsWith("$pkgName:") }.toMutableSet()

           removed.also { it += "$pkgName:$versionCode:$signatureHash" }
       }
    }

    fun revokeAll() {
        sourcePreferences.trustedExtensions().delete()
    }
}
