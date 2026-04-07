package com.example.myapplication.data.model

import com.example.myapplication.data.model.ui.User as UiUser
import com.google.firebase.firestore.DocumentSnapshot

fun User.toUiUser(): UiUser =
    UiUser(
        name = name,
        email = email,
        role = role.toUiUserRole(),
    )

fun DocumentSnapshot.toUser(): User =
    User(
        uid = id,
        name = getString("name").orEmpty(),
        email = getString("email").orEmpty(),
        role = getString("role") ?: "customer",
        createdAt = readMillis("createdAt"),
    )

fun User.toFirestoreMap(): Map<String, Any> =
    mapOf(
        "uid" to uid,
        "name" to name,
        "email" to email,
        "role" to role,
        "createdAt" to createdAt,
    )
