package app.usefriendly.barely

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureNanoTime

class SearchPerformanceTest {
    @Test
    fun thousandItemFuzzySearchStaysInsideTheReleaseLatencyGate() {
        val labels = List(1_000) { index ->
            when (index % 5) {
                0 -> "WhatsApp conversation $index"
                1 -> "Calendar event $index"
                2 -> "Camera shortcut $index"
                3 -> "Banking and payments $index"
                else -> "Notes workspace $index"
            }
        }
        val queries = listOf("whtasapp", "calendar", "cam shrt", "bank", "notes")

        repeat(2) {
            labels.forEach { relevanceScore("warmup", listOf(SearchTerm(it))) }
        }
        val samplesMs = List(20) { sample ->
            measureNanoTime {
                val query = queries[sample % queries.size]
                labels.mapNotNull { label ->
                    relevanceScore(query, listOf(SearchTerm(label)))
                }.sorted()
            } / 1_000_000.0
        }.sorted()
        val p95Ms = samplesMs[(samplesMs.size * 0.95).toInt() - 1]

        println("Barely 1,000-item fuzzy-search p95: $p95Ms ms")
        assertTrue("p95 search latency was $p95Ms ms", p95Ms < 250.0)
    }
}
