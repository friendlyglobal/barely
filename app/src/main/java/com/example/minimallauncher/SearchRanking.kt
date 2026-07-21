package com.example.minimallauncher

import java.text.Normalizer
import java.util.Locale

internal data class SearchTerm(
    val text: String,
    val penalty: Int = 0,
)

internal fun relevanceScore(query: String, terms: List<SearchTerm>): Int? {
    if (query.isBlank()) return null

    return terms.minOfOrNull { term ->
        term.text.matchScore(query)?.plus(term.penalty) ?: Int.MAX_VALUE
    }?.takeUnless { it == Int.MAX_VALUE }
}

internal fun String.normalizedForSearch(): String = Normalizer
    .normalize(this, Normalizer.Form.NFD)
    .replace("\\p{Mn}+".toRegex(), "")
    .lowercase(Locale.getDefault())
    .trim()

private fun String.matchScore(normalizedQuery: String): Int? {
    val normalizedText = normalizedForSearch()
    if (normalizedText.isBlank()) return null

    val textWords = normalizedText.split(SEARCH_WORD_SEPARATOR).filter { it.isNotBlank() }
    val queryWords = normalizedQuery.split(SEARCH_WORD_SEPARATOR).filter { it.isNotBlank() }
    return when {
        normalizedText == normalizedQuery -> 0
        normalizedText.startsWith(normalizedQuery) -> 10
        textWords.any { it.startsWith(normalizedQuery) } -> 20
        normalizedText.contains(normalizedQuery) -> 30
        queryWords.size > 1 -> multiWordScore(queryWords, textWords)
        normalizedQuery.length >= 2 && initialism(textWords).startsWith(normalizedQuery) -> 38
        normalizedQuery.length >= 3 -> fuzzyScore(normalizedQuery, normalizedText, textWords)
        else -> null
    }
}

private fun multiWordScore(queryWords: List<String>, textWords: List<String>): Int? {
    val scores = queryWords.map { queryWord ->
        textWords.minOfOrNull { textWord -> fuzzyWordScore(queryWord, textWord) ?: Int.MAX_VALUE }
            ?.takeUnless { it == Int.MAX_VALUE }
            ?: return null
    }
    return 34 + scores.sum()
}

private fun fuzzyScore(query: String, text: String, words: List<String>): Int? {
    val wordScore = words.minOfOrNull { fuzzyWordScore(query, it) ?: Int.MAX_VALUE }
        ?.takeUnless { it == Int.MAX_VALUE }
    val fullScore = fuzzyWordScore(query, text)
    val subsequenceScore = subsequenceGap(query, text)?.let { 64 + it.coerceAtMost(20) }
    return listOfNotNull(wordScore, fullScore, subsequenceScore).minOrNull()
}

private fun fuzzyWordScore(query: String, candidate: String): Int? {
    if (candidate.startsWith(query)) return 0
    if (candidate.contains(query)) return 8

    val maxDistance = when (query.length) {
        in 0..2 -> 0
        in 3..5 -> 1
        else -> 2
    }
    val distance = damerauLevenshtein(query, candidate, maxDistance)
    return distance?.let { 16 + (it * 8) }
}

private fun damerauLevenshtein(left: String, right: String, limit: Int): Int? {
    if (kotlin.math.abs(left.length - right.length) > limit) return null

    var previousPrevious = IntArray(right.length + 1)
    var previous = IntArray(right.length + 1) { it }
    for (leftIndex in left.indices) {
        val current = IntArray(right.length + 1)
        current[0] = leftIndex + 1
        var rowMinimum = current[0]

        for (rightIndex in right.indices) {
            val substitutionCost = if (left[leftIndex] == right[rightIndex]) 0 else 1
            current[rightIndex + 1] = minOf(
                current[rightIndex] + 1,
                previous[rightIndex + 1] + 1,
                previous[rightIndex] + substitutionCost,
            )
            if (
                leftIndex > 0 && rightIndex > 0 &&
                left[leftIndex] == right[rightIndex - 1] &&
                left[leftIndex - 1] == right[rightIndex]
            ) {
                current[rightIndex + 1] = minOf(
                    current[rightIndex + 1],
                    previousPrevious[rightIndex - 1] + 1,
                )
            }
            rowMinimum = minOf(rowMinimum, current[rightIndex + 1])
        }

        if (rowMinimum > limit) return null
        previousPrevious = previous
        previous = current
    }

    return previous[right.length].takeIf { it <= limit }
}

private fun initialism(words: List<String>): String = buildString {
    words.forEach { word -> word.firstOrNull()?.let(::append) }
}

private fun subsequenceGap(query: String, candidate: String): Int? {
    var queryIndex = 0
    var firstMatch = -1
    var lastMatch = -1
    candidate.forEachIndexed { candidateIndex, character ->
        if (queryIndex < query.length && character == query[queryIndex]) {
            if (firstMatch == -1) firstMatch = candidateIndex
            lastMatch = candidateIndex
            queryIndex++
        }
    }
    if (queryIndex != query.length) return null
    return (lastMatch - firstMatch + 1) - query.length
}

private val SEARCH_WORD_SEPARATOR = "[^\\p{L}\\p{N}]+".toRegex()
