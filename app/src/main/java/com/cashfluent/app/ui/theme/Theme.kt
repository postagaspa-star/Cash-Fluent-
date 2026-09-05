package com.cashfluent.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LightMaterialScheme = lightColorScheme(
    primary = GrowLight,
    onPrimary = SurfaceLight,
    primaryContainer = GrowSoftLight,
    onPrimaryContainer = GrowInkLight,
    secondary = GoldLight,
    onSecondary = SurfaceLight,
    secondaryContainer = GoldSoftLight,
    onSecondaryContainer = GoldInkLight,
    error = CostLight,
    onError = SurfaceLight,
    errorContainer = CostSoftLight,
    onErrorContainer = CostInkLight,
    background = PaperLight,
    onBackground = InkLight,
    surface = SurfaceLight,
    onSurface = InkLight,
    surfaceVariant = SurfaceAltLight,
    onSurfaceVariant = InkSecondaryLight,
    outline = LineStrongLight,
    outlineVariant = LineLight,
)

private val DarkMaterialScheme = darkColorScheme(
    primary = GrowDark,
    onPrimary = PaperDark,
    primaryContainer = GrowSoftDark,
    onPrimaryContainer = GrowInkDark,
    secondary = GoldDark,
    onSecondary = PaperDark,
    secondaryContainer = GoldSoftDark,
    onSecondaryContainer = GoldInkDark,
    error = CostDark,
    onError = PaperDark,
    errorContainer = CostSoftDark,
    onErrorContainer = CostInkDark,
    background = PaperDark,
    onBackground = InkDark,
    surface = SurfaceDark,
    onSurface = InkDark,
    surfaceVariant = SurfaceAltDark,
    onSurfaceVariant = InkSecondaryDark,
    outline = LineStrongDark,
    outlineVariant = LineDark,
)

val LocalCashfluentColors = staticCompositionLocalOf { LightCashfluentColors }

/**
 * Note what is missing: dynamic colour. Green means "what you keep" and clay means
 * "what it costs you" throughout the app, so letting the system replace those hues with
 * whatever is on the user's wallpaper would quietly delete the meaning.
 */
@Composable
fun CashfluentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkCashfluentColors else LightCashfluentColors
    CompositionLocalProvider(LocalCashfluentColors provides colors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkMaterialScheme else LightMaterialScheme,
            typography = CashfluentTypography,
            content = content,
        )
    }
}

object CashfluentTheme {
    val colors: CashfluentColors
        @Composable @ReadOnlyComposable get() = LocalCashfluentColors.current
}
