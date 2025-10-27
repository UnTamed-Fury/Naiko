package eu.kanade.tachiyomi.extension.model

import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import yokai.i18n.MR

enum class InstalledExtensionsOrder(val value: Int, val nameRes: StringResource) {
    Name(0, MR.strings.name),
    RecentlyUpdated(1, MR.strings.recently_updated),
    RecentlyInstalled(2, MR.strings.recently_installed),
    Language(3, MR.strings.language),
    ;

    companion object {
        fun fromValue(preference: Int) = entries.find { it.value == preference } ?: Name
        fun fromPreference(pref: PreferencesHelper) = fromValue(pref.installedExtensionsOrder().get())
    }
}
