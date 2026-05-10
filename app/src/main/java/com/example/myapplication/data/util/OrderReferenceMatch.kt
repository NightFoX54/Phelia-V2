package com.example.myapplication.data.util

import java.util.Locale

/** Matches full Firestore order document ids and short labels like `ORD-Q1DSW8WZ`. */
fun orderReferenceMatchesSearch(orderId: String, rawQuery: String): Boolean {
    val q = rawQuery.trim()
    if (q.isEmpty()) return true
    if (orderId.contains(q, ignoreCase = true)) return true
    val tail = orderId.takeLast(8).uppercase(Locale.US)
    val compact = q.uppercase(Locale.US).replace("\\s+".toRegex(), "")
    val suffix = compact.removePrefix("ORD-").trim()
    val label = "ORD-$tail"
    if (label.contains(q, ignoreCase = true)) return true
    if (suffix.length >= 2) {
        if (tail.contains(suffix)) return true
        if (tail.startsWith(suffix)) return true
    }
    return false
}
