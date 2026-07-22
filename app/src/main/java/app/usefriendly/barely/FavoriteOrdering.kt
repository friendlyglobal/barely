package app.usefriendly.barely

import kotlin.math.ln

internal data class FavoriteUsage(
    val count: Int,
    val lastUsedAt: Long,
)

internal fun reconcileFavoriteOrder(
    storedOrder: List<String>,
    favoriteKeys: Collection<String>,
): List<String> {
    val remaining = favoriteKeys.toMutableSet()
    return buildList {
        storedOrder.forEach { key ->
            if (remaining.remove(key)) add(key)
        }
        addAll(remaining.sorted())
    }
}

internal fun rankFavoriteKeys(
    keys: Collection<String>,
    usage: Map<String, FavoriteUsage>,
    now: Long,
): List<String> = keys.sortedWith(
    compareByDescending<String> { key ->
        val record = usage[key] ?: FavoriteUsage(0, 0L)
        val ageHours = if (record.lastUsedAt > 0L) {
            (now - record.lastUsedAt).coerceAtLeast(0L) / 3_600_000.0
        } else {
            Double.POSITIVE_INFINITY
        }
        val recency = when {
            ageHours < 3 -> 8.0
            ageHours < 24 -> 5.0
            ageHours < 72 -> 3.0
            ageHours < 168 -> 1.0
            else -> 0.0
        }
        ln(record.count.coerceAtLeast(0) + 1.0) * 2.0 + recency
    }.thenBy { it },
)
