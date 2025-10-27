package eu.kanade.tachiyomi.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

fun Context.showNotificationPermissionPrompt(
    requestNotificationPermissionLauncher: ActivityResultLauncher<String>,
    showAnyway: Boolean = false,
    preferences: PreferencesHelper = Injekt.get(),
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val notificationPermission = Manifest.permission.POST_NOTIFICATIONS
    val hasPermission = this.checkSelfPermission(notificationPermission)
    if (hasPermission != PackageManager.PERMISSION_GRANTED &&
        (!preferences.hasShownNotifPermission().get() || showAnyway)
    ) {
        preferences.hasShownNotifPermission().set(true)
        requestNotificationPermissionLauncher.launch((notificationPermission))
    }
}
