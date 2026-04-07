package com.example.myapplication.data.model

import com.example.myapplication.data.model.ui.UserRole

fun String.toUiUserRole(): UserRole = when (lowercase()) {
    "admin" -> UserRole.ADMIN
    "store_owner" -> UserRole.STORE_OWNER
    else -> UserRole.CUSTOMER
}
