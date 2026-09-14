package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.domain.model.SalimTheme
import com.example.domain.model.ThemeMode

private fun createDarkSlateScheme() = darkColorScheme(
    primary = SalimGreen,
    onPrimary = Color.Black,
    primaryContainer = SalimGreenContainer,
    onPrimaryContainer = Color.White,
    secondary = Slate400,
    onSecondary = Slate900,
    background = Slate900,
    onBackground = Slate50,
    surface = Slate800,
    onSurface = Slate50,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate400,
    outline = Slate600
)

private fun createLightSlateScheme() = lightColorScheme(
    primary = SalimGreenLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = Slate600,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder
)

private fun createMidnightVioletScheme() = darkColorScheme(
    primary = VioletAccent,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF3B1E78),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFC4B5FD),
    onSecondary = Color.Black,
    background = Color(0xFF0A0518),
    onBackground = Color(0xFFF5F3FF),
    surface = Color(0xFF160F2E),
    onSurface = Color(0xFFEDE9FE),
    surfaceVariant = Color(0xFF281C4F),
    onSurfaceVariant = Color(0xFFA78BFA),
    outline = Color(0xFF4C3682)
)

private fun createOledBlackScheme() = darkColorScheme(
    primary = SalimGreen,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003820),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFA0A0A0),
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF101010),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1C1C1C),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF333333)
)

private fun createSunsetAmberScheme() = darkColorScheme(
    primary = AmberAccent,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF4D2A00),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFFCD34D),
    onSecondary = Color.Black,
    background = Color(0xFF140E0A),
    onBackground = Color(0xFFFFFBEB),
    surface = Color(0xFF241913),
    onSurface = Color(0xFFFEF3C7),
    surfaceVariant = Color(0xFF3B281E),
    onSurfaceVariant = Color(0xFFFBBF24),
    outline = Color(0xFF6B452B)
)

private fun createForestGreenScheme() = darkColorScheme(
    primary = ForestAccent,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0B3818),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF86EFAC),
    onSecondary = Color.Black,
    background = Color(0xFF07140B),
    onBackground = Color(0xFFF0FDF4),
    surface = Color(0xFF0F2617),
    onSurface = Color(0xFFDCFCE7),
    surfaceVariant = Color(0xFF1C3D27),
    onSurfaceVariant = Color(0xFF4ADE80),
    outline = Color(0xFF2F6641)
)

private fun createOceanBlueScheme() = darkColorScheme(
    primary = OceanAccent,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF073859),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF7DD3FC),
    onSecondary = Color.Black,
    background = Color(0xFF051221),
    onBackground = Color(0xFFF0F9FF),
    surface = Color(0xFF0C2138),
    onSurface = Color(0xFFE0F2FE),
    surfaceVariant = Color(0xFF163557),
    onSurfaceVariant = Color(0xFF38BDF8),
    outline = Color(0xFF245385)
)

private fun createAuroraGlassScheme() = darkColorScheme(
    primary = Color(0xFF2DD4BF),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0F3D39),
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = Color(0xFFC084FC),
    onSecondary = Color.Black,
    background = Color(0xFF090D16),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155)
)

private fun createCyberNeonScheme() = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003F47),
    onPrimaryContainer = Color(0xFFE0FFFF),
    secondary = NeonMagenta,
    onSecondary = Color.White,
    background = Color(0xFF07060E),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF100F1D),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1B1930),
    onSurfaceVariant = Color(0xFFC4B5FD),
    outline = Color(0xFF3D3268)
)

private fun createRoseQuartzScheme() = darkColorScheme(
    primary = Color(0xFFFB7185),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF4C0519),
    onPrimaryContainer = Color(0xFFFFE4E6),
    secondary = Color(0xFFF472B6),
    onSecondary = Color.Black,
    background = Color(0xFF12070A),
    onBackground = Color(0xFFFFF1F2),
    surface = Color(0xFF1F0D13),
    onSurface = Color(0xFFFFF1F2),
    surfaceVariant = Color(0xFF3B121F),
    onSurfaceVariant = Color(0xFFFDA4AF),
    outline = Color(0xFF881337)
)

private fun createLiquidEmeraldScheme() = darkColorScheme(
    primary = Color(0xFF10B981),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF022C22),
    onPrimaryContainer = Color(0xFFD1FAE5),
    secondary = Color(0xFF34D399),
    onSecondary = Color.Black,
    background = Color(0xFF02120C),
    onBackground = Color(0xFFECFDF5),
    surface = Color(0xFF062319),
    onSurface = Color(0xFFECFDF5),
    surfaceVariant = Color(0xFF0A3B2B),
    onSurfaceVariant = Color(0xFF6EE7B7),
    outline = Color(0xFF047857)
)

private fun createSunsetHorizonScheme() = darkColorScheme(
    primary = Color(0xFFFF7A00),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF4A1800),
    onPrimaryContainer = Color(0xFFFFEDD5),
    secondary = Color(0xFFF59E0B),
    onSecondary = Color.Black,
    background = Color(0xFF130904),
    onBackground = Color(0xFFFFF7ED),
    surface = Color(0xFF221107),
    onSurface = Color(0xFFFFF7ED),
    surfaceVariant = Color(0xFF381C0D),
    onSurfaceVariant = Color(0xFFFDBA74),
    outline = Color(0xFF9A3412)
)

@Composable
fun SalimMusicPlayerTheme(
    theme: SalimTheme = SalimTheme.DARK_SLATE,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        !isDark -> createLightSlateScheme()
        else -> when (theme) {
            SalimTheme.DARK_SLATE -> createDarkSlateScheme()
            SalimTheme.LIGHT_SLATE -> createLightSlateScheme()
            SalimTheme.MIDNIGHT_VIOLET -> createMidnightVioletScheme()
            SalimTheme.OLED_PITCH_BLACK -> createOledBlackScheme()
            SalimTheme.SUNSET_AMBER -> createSunsetAmberScheme()
            SalimTheme.FOREST_GREEN -> createForestGreenScheme()
            SalimTheme.OCEAN_BLUE -> createOceanBlueScheme()
            SalimTheme.AURORA_GLASS -> createAuroraGlassScheme()
            SalimTheme.CYBER_NEON -> createCyberNeonScheme()
            SalimTheme.ROSE_QUARTZ -> createRoseQuartzScheme()
            SalimTheme.LIQUID_EMERALD -> createLiquidEmeraldScheme()
            SalimTheme.SUNSET_HORIZON -> createSunsetHorizonScheme()
        }
    }

    val finalScheme = if (highContrast) {
        colorScheme.copy(
            outline = Color.White,
            onBackground = HighContrastText,
            primary = HighContrastGreen
        )
    } else {
        colorScheme
    }

    MaterialTheme(
        colorScheme = finalScheme,
        typography = Typography,
        content = content
    )
}
