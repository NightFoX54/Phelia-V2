package com.example.myapplication.ui

import com.example.myapplication.data.util.orderReferenceMatchesSearch
import java.util.Locale

/** Short user-facing order reference (matches list/chat surfaces); not the raw Firestore document id. */
fun orderPublicLabel(orderId: String): String {
    if (orderId.isBlank()) return "ORD-—"
    val tail = orderId.takeLast(8).uppercase(Locale.US)
    return "ORD-$tail"
}

/** Short label for a line/suborder when parent order id is unknown (clipboard-friendly). */
fun suborderPublicLabel(suborderId: String): String {
    if (suborderId.isBlank()) return "SUB-—"
    val tail = suborderId.takeLast(8).uppercase(Locale.US)
    return "SUB-$tail"
}

/** What to put on the clipboard for an order conversation context (never raw document ids). */
fun clipboardLabelForChat(parentOrderId: String, suborderId: String): String =
    if (parentOrderId.isNotBlank()) orderPublicLabel(parentOrderId) else suborderPublicLabel(suborderId)

/** Matches My Orders search (delegates to shared matcher). */
fun orderMatchesSearchQuery(orderId: String, rawQuery: String): Boolean =
    orderReferenceMatchesSearch(orderId, rawQuery)
