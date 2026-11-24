package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val StudioDarkColorScheme = darkColorScheme(
    primary = ElectricIris,
    onPrimary = Color.White,
    primaryContainer = IrisContainer,
    onPrimaryContainer = ElectricIrisBright,
    secondary = StudioMint,
    onSecondary = Color(0xFF05241D),
    secondaryContainer = MintContainer,
    onSecondaryContainer = StudioMint,
    tertiary = WarmCoral,
    onTertiary = Color(0xFF2B0E06),
    tertiaryContainer = CoralContainer,
    onTertiaryContainer = Color(0xFFFFC1AC),
    background = ObsidianCanvas,
    onBackground = IvoryText,
    surface = StudioSurface,
    onSurface = IvoryText,
    surfaceVariant = StudioSurfaceElevated,
    onSurfaceVariant = MutedSlateText,
    surfaceContainerHigh = StudioSurfaceElevated,
    surfaceContainerHighest = StudioSurfaceHighest,
    outline = SubtleOutline,
    outlineVariant = Color(0xFF22283C),
    error = ErrorRose,
    onError = Color.White,
    errorContainer = ErrorRoseContainer,
    onErrorContainer = Color(0xFFFFB4BE)
)

private val StudioLightColorScheme = lightColorScheme(
    primary = DaylightIris,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7E4FF),
    onPrimaryContainer = Color(0xFF1A1066),
    secondary = DaylightMint,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0F5EB),
    onSecondaryContainer = Color(0xFF00382A),
    tertiary = Color(0xFFD85A33),
    onTertiary = Color.White,
    background = DaylightCanvas,
    onBackground = CharcoalText,
    surface = DaylightSurface,
    onSurface = CharcoalText,
    surfaceVariant = DaylightSurfaceElevated,
    onSurfaceVariant = SlateTextLight,
    outline = LightOutline
)

val AuraShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun AuraStudioTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) StudioDarkColorScheme else StudioLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AuraTypography,
        shapes = AuraShapes,
        content = content
    )
}
