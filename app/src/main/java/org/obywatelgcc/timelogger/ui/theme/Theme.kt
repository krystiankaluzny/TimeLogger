package org.obywatelgcc.timelogger.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)


@Immutable
data class ExtendedColors(
    val timerButtonContent: Color,
    val timerToStartButton: Color,
    val timerToStopButton: Color,
    val timerToResumeButton: Color,
    val saveButtonContent: Color,
    val saveButton: Color,
    val saveButtonBorder: Color,
    val suggestionMatch: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        timerButtonContent = Color.Unspecified,
        timerToStartButton = Color.Unspecified,
        timerToStopButton = Color.Unspecified,
        timerToResumeButton = Color.Unspecified,
        saveButtonContent = Color.Unspecified,
        saveButton = Color.Unspecified,
        saveButtonBorder = Color.Unspecified,
        suggestionMatch = Color.Unspecified
    )
}

@Immutable
data class ExtendedValues(
    val selectedColorBorder: Dp,
    val saveButtonBorderGradientStop: Float,
    val textFieldFontSize: TextUnit,
    val dateTimePickerTextFieldFontSize: TextUnit,
    val dateTimePickerOffsetButtonFontSize: TextUnit,
    val durationTextFontSize: TextUnit,
    val loggerButtonFontSize: TextUnit,
    val saveButtonFontSize: TextUnit,
    val loggerButtonHeight: Dp,
    val saveButtonHeight: Dp,
)

val LocalExtendedValues = staticCompositionLocalOf {
    ExtendedValues(
        selectedColorBorder = 0.dp,
        saveButtonBorderGradientStop = 0.8f,
        textFieldFontSize = 16.sp,
        dateTimePickerTextFieldFontSize = 18.sp,
        dateTimePickerOffsetButtonFontSize = 16.sp,
        durationTextFontSize = 20.sp,
        loggerButtonFontSize = 16.sp,
        saveButtonFontSize = 14.sp,
        loggerButtonHeight = 50.dp,
        saveButtonHeight = 70.dp,
    )
}

@Composable
fun TimeLoggerTheme(
    windowSizeClass: WindowSizeClass,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }


    val extendedColors =
        if (darkTheme)
            ExtendedColors(
                timerButtonContent = Color(0xC4CCBCBC),
                timerToStartButton = Color(0xB5476426),
                timerToStopButton = Color(0xD7811E16),
                timerToResumeButton = Color(0xD2143F73),
                saveButtonContent = Color(0xBCD0EEDC),
                saveButton = Color(0xB59256D5),
                saveButtonBorder = Color(0x4D6DC5D3),
                suggestionMatch = Color(0xFFE07727)
            )
        else {
            ExtendedColors(
                timerButtonContent = Color(0xCB050559),
                timerToStartButton = Color(0xB58BC34A),
                timerToStopButton = Color(0xB0E13325),
                timerToResumeButton = Color(0xBF13BED3),
                saveButtonContent = Color(0xEB0F0F10),
                saveButton = Color(0xB7A057FA),
                saveButtonBorder = Color(0x806DC5D3),
                suggestionMatch = Color(0xFF086AB2)
            )
        }

    val extendedValues =

        if (windowSizeClass.isWidthExpanded()) {
            ExtendedValues(
                selectedColorBorder = if (darkTheme) 3.dp else 1.dp,
                saveButtonBorderGradientStop = if (darkTheme) 0.8f else 0.5f,
                textFieldFontSize = 20.sp,
                dateTimePickerTextFieldFontSize = 22.sp,
                dateTimePickerOffsetButtonFontSize = 20.sp,
                durationTextFontSize = 24.sp,
                loggerButtonFontSize = 20.sp,
                saveButtonFontSize = 20.sp,
                loggerButtonHeight = 70.dp,
                saveButtonHeight = 90.dp,
            )
        } else {
            ExtendedValues(
                selectedColorBorder = if (darkTheme) 3.dp else 1.dp,
                saveButtonBorderGradientStop = if (darkTheme) 0.8f else 0.5f,
                textFieldFontSize = 16.sp,
                dateTimePickerTextFieldFontSize = 18.sp,
                dateTimePickerOffsetButtonFontSize = 16.sp,
                durationTextFontSize = 20.sp,
                loggerButtonFontSize = 14.sp,
                saveButtonFontSize = 14.sp,
                loggerButtonHeight = 50.dp,
                saveButtonHeight = 70.dp,
            )
        }

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
        LocalExtendedValues provides extendedValues
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BaseTypography,
            content = content
        )
    }
}

object TimeLoggerTheme {
    val typography: Typography
        @Composable
        get() = MaterialTheme.typography

    val colorScheme: ColorScheme
        @Composable
        get() = MaterialTheme.colorScheme

    val shapes: Shapes
        @Composable
        get() = MaterialTheme.shapes

    val extendedColors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current

    val values: ExtendedValues
        @Composable
        get() = LocalExtendedValues.current
}

fun WindowSizeClass.isWidthExpanded(): Boolean {
    return this.widthSizeClass == WindowWidthSizeClass.Expanded
}