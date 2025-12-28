package eu.kanade.tachiyomi.core.security

import eu.kanade.tachiyomi.core.preference.PreferenceStore

class SecurityPreferences(private val preferenceStore: PreferenceStore) {
    fun useBiometrics() = preferenceStore.getBoolean("use_biometrics", false)
}
