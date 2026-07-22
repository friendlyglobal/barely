package app.usefriendly.barely

import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteOrderingTest {
    @Test
    fun reconcileKeepsStoredOrderDropsRemovedAndAppendsNewFavorites() {
        assertEquals(
            listOf("b", "a", "c"),
            reconcileFavoriteOrder(
                storedOrder = listOf("removed", "b", "a", "b"),
                favoriteKeys = setOf("a", "b", "c"),
            ),
        )
    }

    @Test
    fun rankingUsesFrequencyAndRecencyWithoutLeavingTheDevice() {
        val now = 1_000_000_000L
        assertEquals(
            listOf("recent", "frequent", "unused"),
            rankFavoriteKeys(
                keys = listOf("unused", "frequent", "recent"),
                usage = mapOf(
                    "frequent" to FavoriteUsage(count = 5, lastUsedAt = now - 10 * 3_600_000L),
                    "recent" to FavoriteUsage(count = 1, lastUsedAt = now - 30 * 60_000L),
                ),
                now = now,
            ),
        )
    }
}
