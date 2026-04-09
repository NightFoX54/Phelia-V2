package com.example.myapplication.data.model

/** One day within the rolling 7-day window on the store dashboard. */
data class StoreSalesDayBucket(
    val label: String,
    val revenue: Double,
    val suborderCount: Int,
)

data class StoreWeeklySalesSummary(
    val days: List<StoreSalesDayBucket>,
    val weekTotalRevenue: Double,
    val weekSuborderCount: Int,
    /** Inclusive range label for the subtitle, e.g. "Jan 1 – Jan 7". */
    val rangeLabel: String,
)
