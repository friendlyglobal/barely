package app.usefriendly.barely

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchRankingTest {
    @Test
    fun exactShortcutLabelBeatsOwnerAppMatch() {
        val shortcutScore = relevanceScore(
            query = "gabriel",
            terms = listOf(SearchTerm("Gabriel"), SearchTerm("WhatsApp", 70)),
        )
        val ownerOnlyScore = relevanceScore(
            query = "whatsapp",
            terms = listOf(SearchTerm("Gabriel"), SearchTerm("WhatsApp", 70)),
        )

        assertEquals(0, shortcutScore)
        assertEquals(70, ownerOnlyScore)
    }

    @Test
    fun wordPrefixesBeatLooseContainsMatches() {
        val wordPrefix = relevanceScore("inc", listOf(SearchTerm("New incognito tab")))
        val looseContains = relevanceScore("inc", listOf(SearchTerm("Reincorporate")))

        assertTrue(wordPrefix!! < looseContains!!)
    }

    @Test
    fun searchIgnoresAccentsAndCase() {
        assertEquals(
            0,
            relevanceScore("joao", listOf(SearchTerm("João"))),
        )
    }

    @Test
    fun searchToleratesTransposedCharacters() {
        assertTrue(
            relevanceScore("whtasapp", listOf(SearchTerm("WhatsApp"))) != null,
        )
    }

    @Test
    fun searchMatchesInitials() {
        assertTrue(
            relevanceScore("wb", listOf(SearchTerm("WhatsApp Business"))) != null,
        )
    }

    @Test
    fun opaqueIdsDoNotCreateLooseFuzzyMatches() {
        assertEquals(
            null,
            relevanceScore(
                "chro",
                listOf(SearchTerm("create_event_shortcut", 96, allowFuzzy = false)),
            ),
        )
    }

    @Test
    fun successfulAppSelectionBoostsOnlyTheSameQueryAndApp() {
        val now = 2_000_000_000L
        val learning = listOf(
            LauncherSearchLearning(
                query = "wa",
                targetKey = "app:whatsapp",
                selectionCount = 3,
                lastSelectedAt = now,
            ),
        )

        assertTrue(learnedSearchBoost("wa", "app:whatsapp", learning, now) > 0)
        assertEquals(0, learnedSearchBoost("wh", "app:whatsapp", learning, now))
        assertEquals(0, learnedSearchBoost("wa", "app:whatsapp-business", learning, now))
    }

    @Test
    fun repeatedRecentSelectionsReceiveTheStrongestBoost() {
        val now = 3_000_000_000L
        val recentRepeated = LauncherSearchLearning("ca", "app:camera", 8, now)
        val oldSingle = LauncherSearchLearning(
            "ca",
            "app:calendar",
            1,
            now - (45L * 24 * 3_600_000),
        )

        assertTrue(
            learnedSearchBoost("ca", "app:camera", listOf(recentRepeated), now) >
                learnedSearchBoost("ca", "app:calendar", listOf(oldSingle), now),
        )
    }
}
