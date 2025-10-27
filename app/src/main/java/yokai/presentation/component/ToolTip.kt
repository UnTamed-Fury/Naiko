package yokai.presentation.component

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.ripple
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun ToolTipButton(
    toolTipLabel: String,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    icon: ImageVector? = null,
    painter: Painter? = null,
    isEnabled: Boolean = true,
    enabledTint: Color = MaterialTheme.colorScheme.onSurface,
    buttonClicked: () -> Unit = {},
) {
    require(icon != null || painter != null)

    val haptic = LocalHapticFeedback.current
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip(
                contentColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
            ) {
                Text(modifier = Modifier.padding(4.dp), style = MaterialTheme.typography.bodyLarge, text = toolTipLabel)
            }
        },
        state = rememberTooltipState(),
    ) {
        CombinedClickableIconButton(
            enabled = isEnabled,
            enabledTint = enabledTint,
            modifier = modifier
                .iconButtonCombinedClickable(
                    toolTipLabel = toolTipLabel,
                    onClick = buttonClicked,
                    isEnabled = isEnabled,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                ),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    modifier = iconModifier,
                    contentDescription = toolTipLabel,
                )
            } else {
                Icon(
                    painter = painter!!,
                    modifier = iconModifier,
                    contentDescription = toolTipLabel,
                )
            }
        }
    }
}

@Composable
fun CombinedClickableIconButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    enabledTint: Color,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
        modifier
            .minimumInteractiveComponentSize()
            .size(40.0.dp),
        contentAlignment = Alignment.Center,
    ) {
        val contentColor =
            if (enabled) {
                enabledTint
            } else {
                MaterialTheme.colorScheme.onBackground
                    .copy(alpha = .38f)
            }
        CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
    }
}

fun Modifier.iconButtonCombinedClickable(
    toolTipLabel: String,
    isEnabled: Boolean,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) = composed {
    if (isEnabled) {
        combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(
                bounded = false,
                radius = 40.dp / 2,
            ),
            onClickLabel = toolTipLabel,
            role = Role.Button,
            onClick = onClick,
            onLongClick = onLongClick,
            onDoubleClick = onDoubleClick,
        )
    } else {
        this
    }
}
