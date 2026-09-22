package com.xcloak.spacexpert.ui.theme

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class SpaceXpertColors(
    val glassBackground: Color,
    val glassBorder: Color,
    val backgroundGradient: Brush
)

val LocalSpaceXpertColors = staticCompositionLocalOf {
    SpaceXpertColors(
        glassBackground = Color.Unspecified,
        glassBorder = Color.Unspecified,
        backgroundGradient = Brush.verticalGradient(listOf(Color.Black, Color.Black))
    )
}

private val DarkColors = darkColorScheme(
    primary = CyanAccent,
    onPrimary = SpaceNavy900,
    secondary = CyanAccentDim,
    background = SpaceNavy900,
    surface = SpaceNavy800,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SpaceNavy700,
    error = DangerRed
)

private val LightColors = lightColorScheme(
    primary = CyanAccentDim,
    onPrimary = SurfaceLight,
    secondary = CyanAccent,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    error = DangerRed
)

@Composable
fun SpaceXpertTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // off by default — keeps your brand palette instead of wallpaper-derived colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SpaceXpertTypography
    ) {
        val customColors = if (darkTheme) {
            SpaceXpertColors(
                glassBackground = GlassNavy,
                glassBorder = GlassNavyBorder,
                backgroundGradient = Brush.verticalGradient(
                    listOf(SpaceNavy900, SpaceNavy800, SpaceNavy900)
                )
            )
        } else {
            SpaceXpertColors(
                glassBackground = GlassWhite,
                glassBorder = GlassWhiteBorder,
                backgroundGradient = Brush.verticalGradient(
                    listOf(BackgroundLight, SurfaceLight, BackgroundLight)
                )
            )
        }

        CompositionLocalProvider(
            LocalSpaceXpertColors provides customColors,
            content = content
        )
    }
}