package eu.kanade.tachiyomi.util.compose

import androidx.compose.material3.ColorScheme

val ColorScheme.textHint get() = onBackground.copy(alpha = 0.35f)
