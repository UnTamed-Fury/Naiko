package yokai.util

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import yokai.presentation.theme.SecondaryItemAlpha

fun Modifier.secondaryItemAlpha(): Modifier = this.alpha(SecondaryItemAlpha)

fun Modifier.clickableNoIndication(
    interactionSource: MutableInteractionSource,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) = this.combinedClickable(
    interactionSource = interactionSource,
    indication = null,
    onLongClick = onLongClick,
    onClick = onClick,
)
