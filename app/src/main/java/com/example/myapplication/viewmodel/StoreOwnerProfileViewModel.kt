package com.example.myapplication.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Store
import com.example.myapplication.data.repository.StoreRepository
import com.example.myapplication.util.StoreLogoImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    /**
     * Crops picked image to a centered **1:1** square, uploads to Storage under `stores/{id}/brand/`, returns download URL.
     */
    fun uploadStoreLogoFromGallery(
        context: Context,
        sourceUri: Uri,
        onResult: (Result<String>) -> Unit,
    ) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            onResult(Result.failure(IllegalStateException("Not signed in")))
            return
        }
        viewModelScope.launch {
            val r = withContext(Dispatchers.IO) {
                runCatching {
                    val cropped = StoreLogoImageUtils.cropCenterSquareToJpegFile(context.applicationContext, sourceUri)
                    repository.uploadStoreLogo(uid, cropped).getOrThrow()
                }
            }
            onResult(r)
        }
    }

    fun submitUpdateStore(
        name: String,
        description: String,
        logoUrl: String,
        email: String,
        phone: String,
        taxNumber: String,
        businessAddress: String,
        onResult: (Result<Unit>) -> Unit,
    ) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            onResult(Result.failure(IllegalStateException("Not signed in")))
            return
        }
        viewModelScope.launch {
            val r = repository.submitStoreUpdateRequest(
                ownerId = uid,
                name = name,
                description = description,
                logoUrl = logoUrl,
                email = email,
                phone = phone,
                taxNumber = taxNumber,
                businessAddress = businessAddress
            )
            onResult(r)
        }
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
