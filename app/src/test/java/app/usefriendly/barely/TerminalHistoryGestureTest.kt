package app.usefriendly.barely

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TerminalHistoryGestureTest {
    @Test
    fun dragProgressTracksTheFingerAcrossTheFullTravelDistance() {
        assertEquals(
            0.5f,
            terminalHistoryProgressAfterDrag(
                currentProgress = 0f,
                delta = -500f,
                travelDistance = 1_000f,
            ),
            0f,
        )
        assertEquals(
            0f,
            terminalHistoryProgressAfterDrag(
                currentProgress = 0.25f,
                delta = 500f,
                travelDistance = 1_000f,
            ),
            0f,
        )
    }

    @Test
    fun settleUsesVelocityThenFallsBackToDragDistance() {
        val threshold = 900f

        assertTrue(shouldOpenTerminalHistory(0.1f, -1_000f, threshold))
        assertFalse(shouldOpenTerminalHistory(0.9f, 1_000f, threshold))
        assertTrue(shouldOpenTerminalHistory(0.42f, 0f, threshold))
        assertFalse(shouldOpenTerminalHistory(0.41f, 0f, threshold))
    }
}
