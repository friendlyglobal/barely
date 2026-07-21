package app.usefriendly.barely

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LauncherSettingsTest {
    @Test
    fun terminalAppearanceUsesTheDocumentedRestrainedDefaults() {
        val settings = LauncherSettings()

        assertEquals(LauncherHomeMode.TERMINAL, settings.homeMode)
        assertEquals(BarelyDefaults.TERMINAL_BACKGROUND_COLOR, settings.terminalBackgroundColor)
        assertEquals(
            BarelyDefaults.TERMINAL_BACKGROUND_OPACITY,
            settings.terminalBackgroundOpacity,
            0f,
        )
        assertFalse(settings.terminalTopActionBackdrop)
        assertFalse(settings.terminalAesthetic)
        assertEquals(12, settings.terminalCornerRadius)
    }
}
