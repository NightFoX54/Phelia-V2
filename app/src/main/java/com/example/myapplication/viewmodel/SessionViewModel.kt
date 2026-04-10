package com.example.myapplication.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.toUiUser
import com.example.myapplication.data.model.ui.User
import com.example.myapplication.data.model.ui.UserRole
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.StoreApplicationRepository
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
    private val _authNotice = MutableStateFlow<String?>(null)
    val authNotice: StateFlow<String?> = _authNotice.asStateFlow()

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
                    if (profile.role == "store_owner") {
                        val gate = authRepository.evaluateStoreOwnerGate(uid).getOrElse {
                            authRepository.signOut()
                            _user.value = null
                            _sessionState.value = SessionState.SignedOut
                            return@fold
                        }
                        if (!gate.allowed) {
                            _authNotice.value = gate.message ?: "Store account is not ready yet."
                            authRepository.signOut()
                            _user.value = null
                            _sessionState.value = SessionState.SignedOut
                            return@fold
                        }
                    }
                    val ui = profile.toUiUser()
                    _user.value = ui
                    _sessionState.value = SessionState.SignedIn(ui)
                },
                onFailure = { e ->
                    _user.value = null
                    _sessionState.value = SessionState.ProfileError(
                        e.message ?: "Could not load profile.",
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
            val base = authRepository.signIn(email, password)
            val checked = base.fold(
                onSuccess = {
                    val uid = auth.currentUser?.uid
                    if (uid.isNullOrBlank()) {
                        Result.failure(IllegalStateException("Could not start session."))
                    } else {
                        authRepository.fetchUserProfile(uid).fold(
                            onFailure = {
                                authRepository.signOut()
                                Result.failure(it)
                            },
                            onSuccess = { profile ->
                                if (profile.role == "store_owner") {
                                    val gate = authRepository.evaluateStoreOwnerGate(uid).getOrElse { err ->
                                        authRepository.signOut()
                                        return@fold Result.failure(err)
                                    }
                                    if (!gate.allowed) {
                                        authRepository.signOut()
                                        val msg = gate.message ?: "Store account is not ready."
                                        _authNotice.value = msg
                                        Result.failure(IllegalStateException(msg))
                                    } else {
                                        Result.success(Unit)
                                    }
                                } else {
                                    Result.success(Unit)
                                }
                            },
                        )
                    }
                },
                onFailure = { Result.failure(it) },
            )
            onResult(checked)
        }
    }

    fun register(name: String, email: String, password: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            authRepository.register(name, email, password, role = "customer").fold(
                onSuccess = { onResult(Result.success(Unit)) },
                onFailure = { onResult(Result.failure(it)) },
            )
        }
    }

    /**
     * Registers a customer account, uploads optional square-cropped logo, writes [storeApplications] as pending.
     */
    fun registerStoreApplication(
        context: Context,
        name: String,
        email: String,
        password: String,
        storeName: String,
        storeDescription: String,
        localLogoUri: Uri?,
        onResult: (Result<Unit>) -> Unit,
    ) {
        viewModelScope.launch {
            authRepository.register(name, email, password, role = "store_owner").fold(
                onFailure = { onResult(Result.failure(it)) },
                onSuccess = { uid ->
                    val appRepo = StoreApplicationRepository()
                    runCatching {
                        appRepo.ensureNoPendingApplication(uid).getOrThrow()
                        val logoUrl = if (localLogoUri != null) {
                            appRepo.uploadApplicantStoreLogo(context.applicationContext, uid, localLogoUri).getOrThrow()
                        } else {
                            ""
                        }
                        appRepo.submitApplication(
                            applicantUserId = uid,
                            applicantName = name.trim(),
                            applicantEmail = email.trim(),
                            storeName = storeName,
                            storeDescription = storeDescription,
                            storeLogoUrl = logoUrl,
                        ).getOrThrow()
                    }.fold(
                        onSuccess = {
                            _authNotice.value = "Your application has been submitted and is pending admin approval."
                            authRepository.signOut()
                            onResult(Result.success(Unit))
                        },
                        onFailure = { e ->
                            authRepository.signOut()
                            onResult(Result.failure(e))
                        },
                    )
                },
            )
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun clearAuthNotice() {
        _authNotice.value = null
    }

    fun setRole(role: UserRole) {
        val u = _user.value ?: return
        _user.value = u.copy(role = role)
        _sessionState.value = SessionState.SignedIn(_user.value!!)
    }
}
