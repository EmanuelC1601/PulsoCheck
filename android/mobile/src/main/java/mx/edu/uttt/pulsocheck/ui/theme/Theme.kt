package mx.edu.uttt.pulsocheck.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandTealLight,
    onPrimary = BrandNavy,
    secondary = Color(0xFFA3CCC4),
    onSecondary = BrandNavy,
    tertiary = Color(0xFFFFB4AB),
    onTertiary = Color(0xFF690005),
    background = BrandDarkBackground,
    onBackground = BrandDarkOnSurface,
    surface = BrandDarkSurface,
    onSurface = BrandDarkOnSurface,
    surfaceVariant = BrandDarkSurfaceVariant,
    onSurfaceVariant = BrandDarkOnSurfaceVariant,
    outlineVariant = Color(0xFF44534F),
    secondaryContainer = Color(0xFF28423E),
    onSecondaryContainer = Color(0xFFCDE9E2),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

private val LightColorScheme = lightColorScheme(
    primary = BrandTeal,
    onPrimary = BrandWhite,
    secondary = Color(0xFF406A65),
    onSecondary = BrandWhite,
    tertiary = BrandCoral,
    onTertiary = BrandWhite,
    background = BrandBackground,
    onBackground = BrandNavy,
    surface = BrandWhite,
    onSurface = BrandNavy,
    surfaceVariant = BrandSurfaceVariant,
    onSurfaceVariant = BrandOnSurfaceVariant,
    outlineVariant = BrandOutlineVariant,
    secondaryContainer = Color(0xFFDCEFEA),
    onSecondaryContainer = Color(0xFF153E3A),
    error = Color(0xFFB3261E),
    onError = BrandWhite,
)

@Composable
fun PulsoCheckTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
