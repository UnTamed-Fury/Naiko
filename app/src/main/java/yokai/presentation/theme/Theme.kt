package yokai.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import com.google.accompanist.themeadapter.material3.createMdc3Theme

@Composable
fun YokaiTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current

    val (colourScheme) =
        @Suppress("DEPRECATION")
        createMdc3Theme(
            context = context,
            layoutDirection = LayoutDirection.Rtl,
            setTextColors = true,
            readTypography = false,
        )

    MaterialTheme(
        colorScheme = colourScheme!!,
        content = content
    )
}
