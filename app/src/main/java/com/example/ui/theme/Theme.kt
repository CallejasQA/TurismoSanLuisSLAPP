package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = VerdeBosque,
    onPrimary = Color.White,
    primaryContainer = VerdeClaro,
    onPrimaryContainer = VerdeProfundo,
    secondary = VerdeProfundo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEF6EC),
    onSecondaryContainer = VerdeProfundo,
    tertiary = VerdeSuave,
    onTertiary = TextoPrincipal,
    background = FondoCremaHoja,
    onBackground = TextoPrincipal,
    surface = FondoSuperficie,
    onSurface = TextoPrincipal,
    surfaceVariant = Color(0xFFEAF3EC),
    onSurfaceVariant = TextoSecundario,
    outline = BordeOrganico,
    outlineVariant = BordeSuave,
    error = PillDangerFg,
    onError = Color.White,
    errorContainer = PillDangerBg,
    onErrorContainer = PillDangerFg
)

private val DarkColorScheme = darkColorScheme(
    primary = VerdeSuave,
    onPrimary = DarkBackground,
    primaryContainer = VerdeProfundo,
    onPrimaryContainer = VerdeClaro,
    secondary = Color(0xFFA5D6A7),
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkTextPrimary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = Color(0xFF395440),
    outlineVariant = Color(0xFF283D2D)
)

val TurismoShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TurismoSanLuisTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = TurismoTypography,
        shapes = TurismoShapes,
        content = content
    )
}
