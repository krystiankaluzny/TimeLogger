package org.obywatelgcc.timelogger.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

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
    val timerToResumeButton: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        timerButtonContent = Color.Unspecified,
        timerToStartButton = Color.Unspecified,
        timerToStopButton = Color.Unspecified,
        timerToResumeButton = Color.Unspecified
    )
}

@Composable
fun TimeLoggerTheme(
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
                timerToResumeButton = Color(0xD2143F73)
            )
        else {
            ExtendedColors(
                timerButtonContent = Color(0xCB050559),
                timerToStartButton = Color(0xB58BC34A),
                timerToStopButton = Color(0xB0E13325),
                timerToResumeButton = Color(0xBF13BED3)
            )
        }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object TimeLoggerTheme {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}