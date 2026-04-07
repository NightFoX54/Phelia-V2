package com.example.myapplication.data.model

data class Category(
    val categoryId: String = "",
    val name: String = "",
    val variantAttributes: List<String> = emptyList(),
)
