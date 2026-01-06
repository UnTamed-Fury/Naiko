package yokai.util

import kotlin.math.max
import kotlin.math.min

/**
 * Modified version of ademar111190's Levenshtein implementation
 *
 * REF: https://gist.github.com/ademar111190/34d3de41308389a0d0d8
 */
fun levenshteinDistance(lhs : CharSequence, rhs : CharSequence): Int {
    if (lhs == rhs) return 0
    if (lhs.isEmpty()) return rhs.length
    if (rhs.isEmpty()) return lhs.length

    val lhsLength = lhs.length + 1
    val rhsLength = rhs.length + 1

    var cost = Array(lhsLength) { it }
    var newCost = Array(lhsLength) { 0 }

    for (i in 1..<rhsLength) {
        newCost[0] = i

        var minCost = i

        for (j in 1..<lhsLength) {
            val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1

            val costReplace = cost[j - 1] + match
            val costInsert = cost[j] + 1
            val costDelete = newCost[j - 1] + 1

            newCost[j] = min(min(costInsert, costDelete), costReplace)
            minCost = min(minCost, newCost[j])
        }

        // Hardcode limit to integer limit, just in case
        if (minCost >= Int.MAX_VALUE) return Int.MAX_VALUE

        val swap = cost
        cost = newCost
        newCost = swap
    }

    return cost.last()
}

fun normalizedLevenshteinSimilarity(lhs : CharSequence, rhs : CharSequence): Double {
    val distance by lazy {
        val maxLength = max(lhs.length, rhs.length)
        if (maxLength == 0) return@lazy 0.0
        levenshteinDistance(lhs, rhs) / maxLength.toDouble()
    }

    return 1.0 - distance
}
