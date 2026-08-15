package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = TextOnDark,
    primaryContainer = PurpleLightContainer,
    onPrimaryContainer = IndigoDeep,
    secondary = IndigoMedium,
    onSecondary = TextOnDark,
    secondaryContainer = SurfaceSubtle,
    onSecondaryContainer = TextPrimary,
    tertiary = IncomeGreen,
    onTertiary = TextOnDark,
    background = LavenderBackground,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceSubtle,
    onSurfaceVariant = TextSecondary,
    outline = PurpleBorder,
    error = ExpenseRed,
    onError = TextOnDark
)

private val DarkColorScheme = darkColorScheme(
    primary = PurpleVibrant,
    onPrimary = TextOnDark,
    primaryContainer = IndigoDeep,
    onPrimaryContainer = PurpleLightContainer,
    secondary = PurplePrimary,
    onSecondary = TextOnDark,
    secondaryContainer = IndigoDark,
    onSecondaryContainer = TextOnDarkSecondary,
    tertiary = IncomeGreen,
    onTertiary = TextOnDark,
    background = IndigoDark,
    onBackground = TextOnDark,
    surface = IndigoDeep,
    onSurface = TextOnDark,
    surfaceVariant = IndigoMedium,
    onSurfaceVariant = TextOnDarkSecondary,
    outline = IndigoMedium,
    error = ExpenseRed,
    onError = TextOnDark
)

@Composable
fun StudentExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
