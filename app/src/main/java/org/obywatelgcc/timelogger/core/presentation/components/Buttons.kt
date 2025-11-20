package org.obywatelgcc.timelogger.core.presentation.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.ButtonElevation
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme

object CustomButtonDefaults {

    val contentModifier = Modifier
        .defaultMinSize(minWidth = ButtonDefaults.MinWidth, minHeight = ButtonDefaults.MinHeight)
        .padding(ButtonDefaults.ContentPadding)

    val textButtonContentModifier = Modifier
        .defaultMinSize(minWidth = ButtonDefaults.MinWidth / 2, minHeight = ButtonDefaults.MinHeight / 2)
        .padding(ButtonDefaults.TextButtonContentPadding)

    val elevatedButtonContentModifier = Modifier
        .defaultMinSize(minWidth = ButtonDefaults.MinWidth, minHeight = ButtonDefaults.MinHeight)
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

    @Composable
    fun buttonElevation(
        defaultElevation: Dp = 0.0.dp,
        pressedElevation: Dp = 0.0.dp,
        focusedElevation: Dp = 0.0.dp,
        hoveredElevation: Dp = 1.0.dp,
        disabledElevation: Dp = 0.0.dp,
    ): CustomButtonElevation =
        CustomButtonElevation(defaultElevation, pressedElevation, focusedElevation, hoveredElevation, disabledElevation)


    @Composable
    fun elevatedButtonElevation(
        defaultElevation: Dp = 1.0.dp,
        pressedElevation: Dp = 1.0.dp,
        focusedElevation: Dp = 1.0.dp,
        hoveredElevation: Dp = 3.0.dp,
        disabledElevation: Dp = 0.0.dp
    ): CustomButtonElevation =
        CustomButtonElevation(defaultElevation, pressedElevation, focusedElevation, hoveredElevation, disabledElevation)

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
    elevation: CustomButtonElevation? = CustomButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentModifier: Modifier = CustomButtonDefaults.contentModifier,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val containerColor = rememberUpdatedState(colors.containerColor(enabled)).value
    val contentColor = rememberUpdatedState(colors.contentColor(enabled)).value
    val shadowElevation = elevation?.shadowElevation(enabled, interactionSource)?.value ?: 0.dp

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
    elevation: CustomButtonElevation? = null,
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
        elevation = elevation,
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
    var contentColor = rememberUpdatedState(colors.contentColor(enabled)).value

    val isPressed by interactionSource.collectIsPressedAsState()
    val isClicked = isPressed || checked
    // Create transition with pressed state
    val transition = updateTransition(targetState = isClicked, label = "button_press_transition")
    fun <T> buttonPressAnimation() = tween<T>(durationMillis = 200, easing = EaseInOut)

    var buttonModifier = modifier

    if (isSystemInDarkTheme()) {

        val clickedBackgroundColor = lerp(containerColor, Color.White, 0.7f)
        val clickedTextColor = lerp(contentColor, Color.Black, 0.7f)
        val backgroundColor by transition.animateColor(
            label = "background_color",
            transitionSpec = { buttonPressAnimation() }) { clicked ->
            if (clicked) clickedBackgroundColor else containerColor
        }

        contentColor = transition.animateColor(
            label = "content_color",
            transitionSpec = { buttonPressAnimation() }) { clicked ->
            if (clicked) clickedTextColor else contentColor
        }.value

        val scale by transition.animateFloat(
            label = "scale",
            transitionSpec = { buttonPressAnimation() }) { clicked ->
            if (clicked) 1.0f else 0.85f
        }

        buttonModifier = buttonModifier
            .minimumInteractiveComponentSize()
            .scale(scale)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .background(color = backgroundColor, shape = shape)
            .clip(shape)
            .semantics { role = Role.Checkbox }
            .toggleable(
                value = checked,
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled,
                onValueChange = onCheckedChange
            )

    } else {
        val dropShadow = rememberUpdatedState(shadowColor.copy(alpha = 0.4f)).value
        val darkDropShadow = rememberUpdatedState(shadowColor.copy(alpha = 0.6f)).value
        val innerShadow = rememberUpdatedState(shadowColor.copy(alpha = 0.5f)).value

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

        buttonModifier = buttonModifier
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
            )
    }

    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides CustomButtonDefaults.minimumInteractiveComponentSize,
        LocalContentColor provides contentColor,
    ) {
        Box(
            modifier = buttonModifier,
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


@Composable
fun ElevatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.elevatedShape,
    colors: ButtonColors = ButtonDefaults.elevatedButtonColors(),
    elevation: CustomButtonElevation? = CustomButtonDefaults.elevatedButtonElevation(),
    border: BorderStroke? = null,
    contentModifier: Modifier = CustomButtonDefaults.elevatedButtonContentModifier,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) =
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentModifier = contentModifier,
        interactionSource = interactionSource,
        content = content
    )


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
