package com.example.myapplication.data.model

data class StoreUpdateRequest(
    val requestId: String = "",
    val storeId: String = "",
    val name: String = "",
    val description: String = "",
    val logo: String = "",
    val email: String = "",
    val phone: String = "",
    val taxNumber: String = "",
    val businessAddress: String = "",
    val status: String = "pending",
    val createdAt: Long = 0L,
)
