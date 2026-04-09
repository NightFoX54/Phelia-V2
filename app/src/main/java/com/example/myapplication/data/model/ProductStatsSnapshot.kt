package com.example.myapplication.data.model

/** Counters from `products/{id}/stats/aggregate`. */
data class ProductStatsSnapshot(
    val views: Long = 0L,
    val addedToCart: Long = 0L,
    val addedToFavorite: Long = 0L,
    val purchased: Long = 0L,
)
