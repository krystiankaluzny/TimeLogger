package org.obywatelgcc.timelogger.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme

object CustomButtonDefaults {

    val contentModifier = Modifier
        .defaultMinSize(minWidth = ButtonDefaults.MinWidth, minHeight = ButtonDefaults.MinHeight)
        .padding(ButtonDefaults.ContentPadding)

    val textButtonContentModifier = Modifier
        .defaultMinSize(minWidth = ButtonDefaults.MinWidth / 2, minHeight = ButtonDefaults.MinHeight / 2)
        .padding(ButtonDefaults.TextButtonContentPadding)

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
    val containerColor = colors.containerColor(enabled)
    val contentColor = colors.contentColor(enabled)
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
