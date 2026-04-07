package com.example.myapplication.viewmodel

import com.example.myapplication.data.model.ui.User

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val user: User) : SessionState
    data class ProfileError(val message: String) : SessionState
}
