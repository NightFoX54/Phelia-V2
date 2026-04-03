package com.example.myapplication.viewmodels

import androidx.lifecycle.ViewModel
import com.example.myapplication.models.User
import com.example.myapplication.models.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionViewModel : ViewModel() {
    private val _user = MutableStateFlow(
        User(
            name = "John Doe",
            email = "user@test.com",
            role = UserRole.CUSTOMER,
        ),
    )
    val user: StateFlow<User> = _user.asStateFlow()

    fun login(email: String, password: String): Boolean {
        val role = when (email.lowercase()) {
            "admin@test.com" -> UserRole.ADMIN
            "store@test.com" -> UserRole.STORE_OWNER
            else -> UserRole.CUSTOMER
        }
        if (password != "123456") return false
        _user.value = User(
            name = when (role) {
                UserRole.ADMIN -> "Admin User"
                UserRole.STORE_OWNER -> "Store Owner"
                UserRole.CUSTOMER -> "John Doe"
            },
            email = email,
            role = role,
        )
        return true
    }

    fun logout() {
        _user.value = User(
            name = "Guest User",
            email = "guest@example.com",
            role = UserRole.CUSTOMER,
        )
    }

    fun setRole(role: UserRole) {
        _user.value = _user.value.copy(role = role)
    }
}

