package app.usefriendly.barely

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF1F1F4),
    onPrimary = Color(0xFF17181C),
    primaryContainer = Color(0xFF303137),
    onPrimaryContainer = Color(0xFFF4F4F6),
    secondary = Color(0xFFD0D0D6),
    onSecondary = Color(0xFF1B1C20),
    secondaryContainer = Color(0xFF2B2C31),
    onSecondaryContainer = Color(0xFFE7E7EA),
    tertiary = Color(0xFFC9C9CF),
    onTertiary = Color(0xFF1D1E22),
    tertiaryContainer = Color(0xFF292A2F),
    onTertiaryContainer = Color(0xFFE5E5E8),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF3A2022),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0F1013),
    onBackground = Color(0xFFF1F1F4),
    surface = Color(0xFF0F1013),
    onSurface = Color(0xFFF1F1F4),
    surfaceVariant = Color(0xFF292A30),
    onSurfaceVariant = Color(0xFFC7C7CE),
    outline = Color(0xFF919198),
    outlineVariant = Color(0xFF45464D),
    surfaceDim = Color(0xFF0B0C0F),
    surfaceBright = Color(0xFF2B2C31),
    surfaceContainerLowest = Color(0xFF090A0C),
    surfaceContainerLow = Color(0xFF131417),
    surfaceContainer = Color(0xFF18191D),
    surfaceContainerHigh = Color(0xFF202126),
    surfaceContainerHighest = Color(0xFF292A30),
)

private val BarelyShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
)

internal object BarelyVisualTokens {
    // Layout: keep text comfortably narrow on phones, foldables, and desktop windows.
    val readableContentMaxWidth = 600.dp
    val screenHorizontalPadding = 24.dp
    val paneHorizontalPadding = 22.dp
    val contentHorizontalPadding = 20.dp
    val controlHorizontalPadding = 18.dp
    val bottomCommandSpacing = 20.dp

    // Shape scale: compact rows -> controls -> cards -> floating panels -> sheets.
    val compactRowShape = RoundedCornerShape(14.dp)
    val controlShape = RoundedCornerShape(16.dp)
    val cardShape = RoundedCornerShape(18.dp)
    val floatingPanelShape = RoundedCornerShape(22.dp)
    val widgetShape = RoundedCornerShape(24.dp)
    val dialogShape = RoundedCornerShape(28.dp)
    val sheetShape = RoundedCornerShape(32.dp)

    // Content and outline hierarchy over wallpaper-backed surfaces.
    const val contentStrong = 0.90f
    const val contentHigh = 0.82f
    const val contentPrimary = 0.78f
    const val contentSecondary = 0.68f
    const val contentMuted = 0.58f
    const val contentFaint = 0.48f
    const val outline = 0.18f
    const val outlineSubtle = 0.12f

    // Translucent graphite surfaces. Wallpaper remains visible beneath every level.
    const val surfaceRaised = 0.68f
    const val surfaceSelected = 0.62f
    const val surfaceControl = 0.58f
    const val surfaceIdle = 0.38f
    const val surfaceSubtle = 0.32f

    val raisedElevation = 6.dp

    const val frostedBlurRadiusDp = 34
    const val searchBlurRadiusDp = 48

    const val pageScrimTopWithBlur = 0.24f
    const val pageScrimMiddleWithBlur = 0.20f
    const val pageScrimBottomWithBlur = 0.38f
    const val pageScrimTopFallback = 0.32f
    const val pageScrimMiddleFallback = 0.30f
    const val pageScrimBottomFallback = 0.52f

    const val searchScrimTopWithBlur = 0.36f
    const val searchScrimMiddleWithBlur = 0.44f
    const val searchScrimBottomWithBlur = 0.60f
    const val searchScrimTopFallback = 0.42f
    const val searchScrimMiddleFallback = 0.50f
    const val searchScrimBottomFallback = 0.66f
}

internal object BarelyMotionTokens {
    const val instant = 100
    const val quick = 120
    const val fast = 140
    const val standard = 180
    const val deliberate = 240
    const val reveal = 300
}

@Composable
fun BarelyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        shapes = BarelyShapes,
        content = content,
    )
}
