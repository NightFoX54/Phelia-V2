package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.toUiUser
import com.example.myapplication.data.model.ui.User
import com.example.myapplication.data.model.ui.UserRole
import com.example.myapplication.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ViewModel() {

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { firebaseUser ->
        if (firebaseUser == null) {
            _user.value = null
            _sessionState.value = SessionState.SignedOut
            return@AuthStateListener
        }
        val uid = firebaseUser.uid
        if (uid.isNullOrBlank()) {
            _user.value = null
            _sessionState.value = SessionState.SignedOut
            return@AuthStateListener
        }
        viewModelScope.launch {
            _sessionState.value = SessionState.Loading
            authRepository.fetchUserProfile(uid).fold(
                onSuccess = { profile ->
                    val ui = profile.toUiUser()
                    _user.value = ui
                    _sessionState.value = SessionState.SignedIn(ui)
                },
                onFailure = { e ->
                    _user.value = null
                    _sessionState.value = SessionState.ProfileError(
                        e.message ?: "Profil yüklenemedi.",
                    )
                },
            )
        }
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authListener)
        super.onCleared()
    }

    fun signIn(email: String, password: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(authRepository.signIn(email, password))
        }
    }

    fun register(name: String, email: String, password: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(authRepository.register(name, email, password))
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun setRole(role: UserRole) {
        val u = _user.value ?: return
        _user.value = u.copy(role = role)
        _sessionState.value = SessionState.SignedIn(_user.value!!)
    }
}
