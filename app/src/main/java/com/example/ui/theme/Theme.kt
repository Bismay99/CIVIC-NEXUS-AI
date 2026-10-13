package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Immutable
data class CivicCustomColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val cardContainer: Color,
    val cardBorder: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accentTeal: Color,
    val accentTealBright: Color,
    val accentCyan: Color,
    val accentBlue: Color,
    val inputBackground: Color,
    val inputBorder: Color,
    val chipBackground: Color,
    val chipBorder: Color,
    val topBarBackground: Color,
    val bottomBarBackground: Color,
    val dividerColor: Color
)

val CivicDarkCustomColors = CivicCustomColors(
    isDark = true,
    background = Color(0xFF080E1E),
    surface = Color(0xFF0F172A),
    cardContainer = Color(0xFF162238),
    cardBorder = Color(0xFF263550),
    surfaceVariant = Color(0xFF1E293B),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    accentTeal = Color(0xFF14B8A6),
    accentTealBright = Color(0xFF2DD4BF),
    accentCyan = Color(0xFF38BDF8),
    accentBlue = Color(0xFF3B82F6),
    inputBackground = Color(0xFF162238),
    inputBorder = Color(0xFF263550),
    chipBackground = Color(0xFF1E293B),
    chipBorder = Color(0xFF263550),
    topBarBackground = Color(0xFF0F172A),
    bottomBarBackground = Color(0xFF0F172A),
    dividerColor = Color(0xFF263550)
)

val CivicLightCustomColors = CivicCustomColors(
    isDark = false,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    cardContainer = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFFF1F5F9),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF64748B),
    accentTeal = Color(0xFF0D9488),
    accentTealBright = Color(0xFF0F766E),
    accentCyan = Color(0xFF0284C7),
    accentBlue = Color(0xFF2563EB),
    inputBackground = Color(0xFFFFFFFF),
    inputBorder = Color(0xFFCBD5E1),
    chipBackground = Color(0xFFF1F5F9),
    chipBorder = Color(0xFFE2E8F0),
    topBarBackground = Color(0xFFFFFFFF),
    bottomBarBackground = Color(0xFFFFFFFF),
    dividerColor = Color(0xFFE2E8F0)
)

val LocalCivicCustomColors = staticCompositionLocalOf {
    CivicDarkCustomColors
}

val MaterialTheme.civicColors: CivicCustomColors
    @Composable
    get() = LocalCivicCustomColors.current

private val CivicNexusDarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF080E1E),
    primaryContainer = Color(0xFF162238),
    onPrimaryContainer = Color(0xFF7DD3FC),
    secondary = Color(0xFF14B8A6),
    onSecondary = Color(0xFF080E1E),
    secondaryContainer = Color(0xFF132D38),
    onSecondaryContainer = Color(0xFF5EEAD4),
    tertiary = Color(0xFF818CF8),
    onTertiary = Color.White,
    background = Color(0xFF080E1E),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF0F172A),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF263550),
    outlineVariant = Color(0xFF1E2D4A),
    error = PriorityCritical,
    onError = Color.White
)

private val CivicNexusLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF0D9488),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF0F766E),
    tertiary = Color(0xFF4F46E5),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CivicNexusDarkColorScheme else CivicNexusLightColorScheme
    val customColors = if (darkTheme) CivicDarkCustomColors else CivicLightCustomColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalCivicCustomColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
