package app.usefriendly.barely

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LauncherSettingsTest {
    @Test
    fun terminalAppearanceUsesTheDocumentedRestrainedDefaults() {
        val settings = LauncherSettings()

        assertEquals(BarelyDefaults.TERMINAL_BACKGROUND_COLOR, settings.terminalBackgroundColor)
        assertEquals(
            BarelyDefaults.TERMINAL_BACKGROUND_OPACITY,
            settings.terminalBackgroundOpacity,
            0f,
        )
        assertFalse(settings.terminalTopActionBackdrop)
        assertEquals(12, settings.terminalCornerRadius)
    }
}
