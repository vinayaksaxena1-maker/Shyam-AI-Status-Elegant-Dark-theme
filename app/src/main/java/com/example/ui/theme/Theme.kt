package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val ElegantDarkColorScheme = darkColorScheme(
    primary = ElegantGold,
    secondary = ElegantSaffron,
    tertiary = ElegantGold,
    background = ElegantDarkBackground,
    surface = ElegantDarkSurface,
    onPrimary = ElegantDarkBackground,
    onSecondary = ElegantDarkBackground,
    onBackground = ElegantDarkOnBackground,
    onSurface = ElegantDarkOnBackground,
    onSurfaceVariant = ElegantDarkTextMuted,
    outline = ElegantDarkBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for the elegant dark design
    dynamicColor: Boolean = false, // Disable dynamic colors to keep our premium golden palette
    content: @Composable () -> Unit,
) {
    // We strictly use our custom Elegant Dark color scheme to adhere to "Elegant Dark" theme requirements
    val colorScheme = ElegantDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
