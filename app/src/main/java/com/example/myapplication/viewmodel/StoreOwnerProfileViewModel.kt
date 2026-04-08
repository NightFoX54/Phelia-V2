package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Store
import com.example.myapplication.data.repository.StoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StoreOwnerProfileViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val repository: StoreRepository = StoreRepository(),
) : ViewModel() {

    private val _store = MutableStateFlow<Store?>(null)
    val store: StateFlow<Store?> = _store.asStateFlow()

    private val _storeLoadError = MutableStateFlow<String?>(null)
    val storeLoadError: StateFlow<String?> = _storeLoadError.asStateFlow()

    private var storeListener: ListenerRegistration? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { user ->
        storeListener?.remove()
        storeListener = null
        val uid = user?.uid
        if (uid.isNullOrBlank()) {
            _store.value = null
            _storeLoadError.value = null
        } else {
            storeListener = repository.listenStoreByOwner(
                ownerId = uid,
                onUpdate = { s ->
                    _store.value = s
                    if (s != null) {
                        _storeLoadError.value = null
                    } else {
                        _storeLoadError.value = "Store record not found. Ask an admin to link your account."
                    }
                },
                onError = { msg ->
                    if (msg != null) {
                        _storeLoadError.value = msg
                    }
                },
            )
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    fun saveStore(
        name: String,
        description: String,
        logoUrl: String,
        onResult: (Result<Unit>) -> Unit,
    ) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            onResult(Result.failure(IllegalStateException("Not signed in")))
            return
        }
        viewModelScope.launch {
            val r = repository.updateStoreForOwner(
                ownerId = uid,
                name = name,
                description = description,
                logoUrl = logoUrl,
            )
            onResult(r)
        }
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authStateListener)
        storeListener?.remove()
        super.onCleared()
    }
}
