package com.example.myapplication.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "customer",
    val createdAt: Long = 0L,
    val phone: String = "",
    val bio: String = "",
)
