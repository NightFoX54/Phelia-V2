package com.example.myapplication.data.model

data class Store(
    val storeId: String = "",
    val ownerId: String = "",
    val name: String = "",
    val description: String = "",
    val logo: String = "",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val createdAt: Long = 0L,
)
