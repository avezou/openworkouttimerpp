package xyz.negmawon.workouttimerpp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/* ---------- colour palette ---------- */
private val mdLight = lightColorScheme(
    primary = Color(0xFF5E35B1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1C4E9),
    onPrimaryContainer = Color(0xFF1E0080),

    secondary = Color(0xFFFFC107),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFFECB3),
    onSecondaryContainer = Color(0xFF3A2F00),

    background = Color(0xFFF6F4FF),
    onBackground = Color(0xFF1C1B1F),

    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),

    error = Color(0xFFB00020),
    onError = Color.White,
)

private val mdDark = darkColorScheme(
    primary = Color(0xFFD1BCFF),
    onPrimary = Color(0xFF32008F),
    primaryContainer = Color(0xFF4B27C5),
    onPrimaryContainer = Color(0xFFEADDFF),

    secondary = Color(0xFFFFDF6B),
    onSecondary = Color(0xFF3A2F00),
    secondaryContainer = Color(0xFF554600),
    onSecondaryContainer = Color(0xFFFFDF6B),

    background = Color(0xFF121212),
    onBackground = Color(0xFFE6E1E5),

    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
)


/* ---------- shapes ---------- */
private val macroShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp)
)

@Composable
fun WorkoutTimerTheme(
    useDark: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (useDark) mdDark else mdLight,
        typography = AppTypography,
        shapes = macroShapes,
        content = content
    )
}
