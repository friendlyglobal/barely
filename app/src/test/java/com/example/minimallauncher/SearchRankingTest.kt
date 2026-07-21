package com.example.minimallauncher

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
}
