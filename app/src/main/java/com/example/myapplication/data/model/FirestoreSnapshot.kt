package com.example.myapplication.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Firestore'da tarih alani [Timestamp], [Long] veya baska sayi tipi olabilir.
 * Uygulama katmaninda milisaniye [Long] kullaniyoruz.
 */
fun DocumentSnapshot.readMillis(field: String = "createdAt"): Long {
    val v = get(field) ?: return 0L
    return when (v) {
        is Number -> v.toLong()
        is Timestamp -> v.toDate().time
        else -> 0L
    }
}
