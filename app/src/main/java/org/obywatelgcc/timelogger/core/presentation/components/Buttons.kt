package org.obywatelgcc.timelogger.core.presentation.components

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.combine
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme

object CustomButtonDefaults {

    val contentModifier = Modifier
        .defaultMinSize(minWidth = ButtonDefaults.MinWidth, minHeight = ButtonDefaults.MinHeight)
        .padding(ButtonDefaults.ContentPadding)

    val textButtonContentModifier = Modifier
        .defaultMinSize(minWidth = ButtonDefaults.MinWidth / 2, minHeight = ButtonDefaults.MinHeight / 2)
        .padding(ButtonDefaults.TextButtonContentPadding)

    @Composable
    fun toggleButtonDefaultColors(): ButtonColors {
        return ButtonColors(
            TimeLoggerTheme.colorScheme.surfaceContainerLow,
            TimeLoggerTheme.colorScheme.primary,
            TimeLoggerTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            TimeLoggerTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }

    val minimumInteractiveComponentSize = 25.dp

    fun Modifier.textButtonContentModifier(paddingValues: PaddingValues) = this
        .defaultMinSize(minWidth = ButtonDefaults.MinWidth / 2, minHeight = ButtonDefaults.MinHeight / 2)
        .padding(paddingValues)

    fun Modifier.textButtonContentModifier(all: Dp) = this
        .defaultMinSize(minWidth = ButtonDefaults.MinWidth / 2, minHeight = ButtonDefaults.MinHeight / 2)
        .padding(all)

    fun Modifier.textButtonContentModifier(horizontal: Dp = 0.dp, vertical: Dp = 0.dp) = this
        .defaultMinSize(minWidth = ButtonDefaults.MinWidth / 2, minHeight = ButtonDefaults.MinHeight / 2)
        .padding(horizontal, vertical)
}

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
//    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentModifier: Modifier = CustomButtonDefaults.contentModifier,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val containerColor = rememberUpdatedState(colors.containerColor(enabled)).value
    val contentColor = rememberUpdatedState(colors.contentColor(enabled)).value
    val shadowElevation = 0.dp

    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides CustomButtonDefaults.minimumInteractiveComponentSize,
    ) {
        Surface(
            onClick = onClick,
            modifier = modifier.semantics { role = Role.Button },
            enabled = enabled,
            shape = shape,
            color = containerColor,
            contentColor = contentColor,
            shadowElevation = shadowElevation,
            border = border,
            interactionSource = interactionSource
        ) {
            ProvideContentColorTextStyle(
                contentColor = contentColor,
                textStyle = TimeLoggerTheme.typography.labelLarge
            ) {
                Row(
                    contentModifier,
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }
        }
    }
}

@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.textShape,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
//    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentModifier: Modifier = CustomButtonDefaults.textButtonContentModifier,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) =
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
//        elevation = elevation,
        border = border,
        contentModifier = contentModifier,
        interactionSource = interactionSource,
        content = content
    )

@Composable
fun ToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ShapeDefaults.Medium,
    colors: ButtonColors = CustomButtonDefaults.toggleButtonDefaultColors(),
    shadowColor: Color = TimeLoggerTheme.colorScheme.outline,
    border: BorderStroke? = null,
    contentModifier: Modifier = CustomButtonDefaults.contentModifier,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val containerColor = rememberUpdatedState(colors.containerColor(enabled)).value
    val contentColor = rememberUpdatedState(colors.contentColor(enabled)).value

    val dropShadow = rememberUpdatedState(shadowColor.copy(alpha = 0.4f)).value
    val darkDropShadow = rememberUpdatedState(shadowColor.copy(alpha = 0.6f)).value
    val innerShadow = rememberUpdatedState(shadowColor.copy(alpha = 0.5f)).value

    val isPressed by interactionSource.collectIsPressedAsState()
    val isClicked = isPressed || checked

    // Create transition with pressed state
    val transition = updateTransition(targetState = isClicked, label = "button_press_transition")

    fun <T> buttonPressAnimation() = tween<T>(durationMillis = 200, easing = EaseInOut)

    val shadowAlpha by transition.animateFloat(
        label = "shadow_alpha",
        transitionSpec = { buttonPressAnimation() }) { clicked ->
        if (clicked) 0f else 1f
    }
    val innerShadowAlpha by transition.animateFloat(
        label = "shadow_alpha",
        transitionSpec = { buttonPressAnimation() }) { clicked ->
        if (clicked) 1f else 0f
    }

    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides CustomButtonDefaults.minimumInteractiveComponentSize,
        LocalContentColor provides contentColor,
    ) {
        Box(
            modifier =
                modifier
                    .minimumInteractiveComponentSize()
                    .then(if (border != null) Modifier.border(border, shape) else Modifier)
                    .dropShadow(
                        shape = shape,
                        shadow = Shadow(
                            radius = 3.dp,
                            spread = 0.dp,
                            color = dropShadow,
                            offset = DpOffset(x = 0.dp, -(2).dp),
                            alpha = shadowAlpha
                        )
                    )
                    .dropShadow(
                        shape = shape,
                        shadow = Shadow(
                            radius = 3.dp,
                            spread = 0.dp,
                            color = darkDropShadow,
                            offset = DpOffset(x = 2.dp, 2.dp),
                            alpha = shadowAlpha
                        )
                    )
                    .background(color = containerColor, shape = shape)
                    .clip(shape)
                    .semantics { role = Role.Checkbox }
                    .innerShadow(
                        shape = shape,
                        shadow = Shadow(
                            radius = 3.dp,
                            spread = 1.dp,
                            color = innerShadow,
                            offset = DpOffset(x = 2.dp, 2.dp),
                            alpha = innerShadowAlpha
                        )
                    )
                    .innerShadow(
                        shape = shape,
                        shadow = Shadow(
                            radius = 1.dp,
                            spread = 1.dp,
                            color = innerShadow,
                            offset = DpOffset(x = 0.dp, 0.dp),
                            alpha = innerShadowAlpha
                        )
                    )
                    .toggleable(
                        value = checked,
                        interactionSource = interactionSource,
                        indication = ripple(),
                        enabled = enabled,
                        onValueChange = onCheckedChange
                    ),
            propagateMinConstraints = true
        ) {
            ProvideContentColorTextStyle(
                contentColor = contentColor,
                textStyle = TimeLoggerTheme.typography.labelLarge
            ) {
                Row(
                    contentModifier,
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }
        }
    }
}


fun ButtonColors.containerColor(enabled: Boolean): Color =
    if (enabled) containerColor else disabledContainerColor

fun ButtonColors.contentColor(enabled: Boolean): Color =
    if (enabled) contentColor else disabledContentColor

@Composable
internal fun ProvideContentColorTextStyle(
    contentColor: Color,
    textStyle: TextStyle,
    content: @Composable () -> Unit
) {
    val mergedStyle = LocalTextStyle.current.merge(textStyle)
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalTextStyle provides mergedStyle,
        content = content
    )
}
