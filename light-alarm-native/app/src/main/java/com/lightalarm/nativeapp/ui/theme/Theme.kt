package com.lightalarm.nativeapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.lightalarm.nativeapp.data.LightTheme

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    secondary = AccentOrange,
    tertiary = PinkOceanEnd
)

private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    secondary = AccentOrange,
    tertiary = PinkOceanEnd
)

@Composable
fun LightAlarmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    selectedTheme: LightTheme = LightTheme.SUNRISE,
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Helper function to get theme gradient colors
@Composable
fun getThemeGradientColors(theme: LightTheme, isDark: Boolean = false): Pair<Color, Color> {
    return when (theme) {
        LightTheme.SUNRISE -> if (isDark) Pair(SunriseEnd, SunriseStart) else Pair(SunriseStart, SunriseEnd)
        LightTheme.GREEN_GRASS -> if (isDark) Pair(GreenGrassEnd, GreenGrassStart) else Pair(GreenGrassStart, GreenGrassEnd)
        LightTheme.BLUE_SEA -> if (isDark) Pair(BlueSeaEnd, BlueSeaStart) else Pair(BlueSeaStart, BlueSeaEnd)
        LightTheme.SEPHORA_BLUE -> if (isDark) Pair(SephoraBlueEnd, SephoraBlueStart) else Pair(SephoraBlueStart, SephoraBlueEnd)
        LightTheme.PASTEL_GREEN -> if (isDark) Pair(PastelGreenEnd, PastelGreenStart) else Pair(PastelGreenStart, PastelGreenEnd)
        LightTheme.AURORA -> if (isDark) Pair(AuroraEnd, AuroraStart) else Pair(AuroraStart, AuroraEnd)
        LightTheme.PINK_OCEAN -> if (isDark) Pair(PinkOceanEnd, PinkOceanStart) else Pair(PinkOceanStart, PinkOceanEnd)
        LightTheme.LAVENDER -> if (isDark) Pair(LavenderEnd, LavenderStart) else Pair(LavenderStart, LavenderEnd)
        LightTheme.ALL_NIGHT -> if (isDark) Pair(AllNightEnd, AllNightStart) else Pair(AllNightStart, AllNightEnd)
    }
}
