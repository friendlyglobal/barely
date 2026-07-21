package app.usefriendly.barely

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB8C7FF),
    onPrimary = Color(0xFF1B2B52),
    primaryContainer = Color(0xFF293A61),
    onPrimaryContainer = Color(0xFFD9E2FF),
    secondary = Color(0xFFC3C6DD),
    secondaryContainer = Color(0xFF35384A),
    tertiaryContainer = Color(0xFF4A334B),
    background = Color(0xFF111318),
    surface = Color(0xFF111318),
    surfaceVariant = Color(0xFF292A31),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF40558A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E2FF),
    onPrimaryContainer = Color(0xFF273E70),
    secondaryContainer = Color(0xFFE1E2F7),
    tertiaryContainer = Color(0xFFFFD7F5),
    background = Color(0xFFFAF8FF),
    surface = Color(0xFFFAF8FF),
    surfaceVariant = Color(0xFFE3E2E9),
)

@Composable
fun BarelyTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && dark -> dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        content = content,
    )
}
