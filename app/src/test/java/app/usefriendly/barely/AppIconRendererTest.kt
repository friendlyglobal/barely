package app.usefriendly.barely

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppIconRendererTest {
    @Test
    fun legacyArtworkUsesProgressivelySaferSpacingForStricterMasks() {
        val original = with(AppIconRenderer) { AppIconShape.ORIGINAL.legacyContentScale() }
        val circle = with(AppIconRenderer) { AppIconShape.CIRCLE.legacyContentScale() }
        val squircle = with(AppIconRenderer) { AppIconShape.SQUIRCLE.legacyContentScale() }
        val rounded = with(AppIconRenderer) { AppIconShape.ROUNDED_SQUARE.legacyContentScale() }

        assertEquals(1f, original, 0f)
        assertTrue(circle < squircle)
        assertTrue(squircle < rounded)
        assertTrue(rounded < original)
    }
}
