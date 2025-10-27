package yokai.presentation.component.preference.widget

import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun SliderPreferenceWidget(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    value: Int,
    min: Int,
    max: Int,
    onValueChange: (Float) -> Unit,
) {
    TextPreferenceWidget(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        icon = icon,
        widget = {
            Slider(
                value = value.toFloat(),
                onValueChange = onValueChange,
                valueRange = min.toFloat()..max.toFloat()
            )
        },
    )
}
