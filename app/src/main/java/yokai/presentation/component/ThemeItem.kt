package yokai.presentation.component

import android.R as AR
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.google.accompanist.themeadapter.material3.createMdc3Theme
import eu.kanade.tachiyomi.R
import yokai.i18n.MR
import yokai.util.lang.getString
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.util.system.Themes
import eu.kanade.tachiyomi.util.system.isInNightMode
import yokai.presentation.theme.HalfAlpha
import yokai.presentation.theme.SecondaryItemAlpha
import yokai.presentation.theme.Size

private data class ContextTheme(
    val colorScheme: ColorScheme,
    val isThemeMatchesApp: Boolean,
    val theme: Themes,
    val isDarkTheme: Boolean,
)

private fun Context.colorSchemeFromAdapter(theme: Themes, isDarkTheme: Boolean): ContextTheme {
    val configuration = Configuration(this.resources.configuration)
    configuration.uiMode =
        if (isDarkTheme) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
    val themeContext = this.createConfigurationContext(configuration)
    themeContext.setTheme(theme.styleRes)

    @Suppress("DEPRECATION") val colorScheme =
        createMdc3Theme(
            context = themeContext,
            layoutDirection = LayoutDirection.Ltr,
            setTextColors = true,
            readTypography = false,
        )
            .colorScheme!!

    val themeMatchesApp =
        if (this.isInNightMode()) {
            isDarkTheme
        } else {
            !isDarkTheme
        }

    return ContextTheme(colorScheme, themeMatchesApp, theme, isDarkTheme)
}

@Composable
fun ThemeItem(theme: Themes, isDarkTheme: Boolean, selected: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val contextTheme = context.colorSchemeFromAdapter(theme, isDarkTheme)

    ThemeItemNaive(contextTheme = contextTheme, selected = selected, onClick = onClick)
}

@Composable
private fun ThemeItemNaive(contextTheme: ContextTheme, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(110.dp)) {
        ThemePreviewItem(
            contextTheme.colorScheme,
            selected,
            selectedColor = MaterialTheme.colorScheme.primary,
            contextTheme.isThemeMatchesApp,
            onClick
        )

        Text(
            text = stringResource(if (contextTheme.isDarkTheme) contextTheme.theme.darkNameRes else contextTheme.theme.nameRes),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun ThemePreviewItem(
    colorScheme: ColorScheme,
    selected: Boolean,
    selectedColor: Color,
    themeMatchesApp: Boolean,
    onClick: () -> Unit,
) {
    val actualSelectedColor =
        when {
            themeMatchesApp && selected -> colorScheme.primary
            selected -> selectedColor.copy(alpha = HalfAlpha)
            else -> Color.Transparent
        }

    val padding = 6
    val outer = 26
    val inner = outer - padding
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier
            .height(180.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(outer.dp),
        border = BorderStroke(width = Size.tiny, color = actualSelectedColor),
    ) {
        OutlinedCard(
            modifier = Modifier
                .height(180.dp)
                .fillMaxWidth()
                .padding(6.dp),
            shape = RoundedCornerShape(inner.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = colorScheme.background),
            border = BorderStroke(width = 1.dp, color = colorScheme.surfaceVariant),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(Size.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .height(15.dp)
                        .weight(0.7f)
                        .padding(start = Size.tiny, end = Size.small)
                        .background(
                            color = colorScheme.onSurface,
                            shape = RoundedCornerShape(6.dp),
                        ),
                )
                Box(
                    modifier = Modifier.weight(0.3f),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = stringResource(MR.strings.selected),
                            tint = selectedColor
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .weight(0.6f)
                    .padding(horizontal = Size.small),
            ) {
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .fillMaxWidth()
                        .background(
                            color = colorScheme.onSurface.copy(alpha = SecondaryItemAlpha),
                            shape = RoundedCornerShape(8.dp),
                        ),
                )
                Row(
                    modifier = Modifier
                        .height(30.dp)
                        .fillMaxWidth()
                        .padding(top = Size.small, end = Size.small),
                ) {
                    Box(
                        modifier = Modifier
                            .height(15.dp)
                            .weight(0.8f)
                            .padding(end = Size.tiny)
                            .background(
                                color = colorScheme.onSurface,
                                shape = RoundedCornerShape(6.dp),
                            ),
                    )

                    Box(
                        modifier = Modifier
                            .height(15.dp)
                            .weight(0.3f)
                            .background(
                                color = colorScheme.secondary,
                                shape = RoundedCornerShape(6.dp),
                            ),
                    )
                }
                Row(
                    modifier = Modifier
                        .height(15.dp)
                        .fillMaxWidth()
                        .padding(end = Size.medium),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.5f)
                            .padding(end = Size.tiny)
                            .background(
                                color = colorScheme.onSurface,
                                shape = RoundedCornerShape(6.dp),
                            ),
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.6f)
                            .background(
                                color = colorScheme.onSurface,
                                shape = RoundedCornerShape(6.dp),
                            ),
                    )
                }
            }
            Surface(
                color = colorScheme.surfaceVariant,
                tonalElevation = Size.small,
            ) {
                Row(
                    modifier = Modifier
                        .height(30.dp)
                        .fillMaxWidth()
                        .padding(vertical = Size.extraTiny, horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.2f),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp)
                                .background(
                                    color = colorScheme.onSurface.copy(alpha = SecondaryItemAlpha),
                                    shape = CircleShape,
                                ),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.2f),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp)
                                .background(
                                    color = colorScheme.secondary,
                                    shape = CircleShape,
                                ),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.2f),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp)
                                .background(
                                    color = colorScheme.onSurface.copy(alpha = SecondaryItemAlpha),
                                    shape = CircleShape,
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun ThemeItemPreviewDark() {
    val contextTheme = ContextTheme(
        colorScheme = darkColorScheme(),
        isThemeMatchesApp = true,
        theme = Themes.DEFAULT,
        isDarkTheme = true,
    )
    Surface {
        ThemeItemNaive(contextTheme = contextTheme, selected = true) {}
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
fun ThemeItemPreviewLight() {
    val contextTheme = ContextTheme(
        colorScheme = lightColorScheme(),
        isThemeMatchesApp = true,
        theme = Themes.DEFAULT,
        isDarkTheme = false,
    )
    Surface {
        ThemeItemNaive(contextTheme = contextTheme, selected = false) {}
    }
}
