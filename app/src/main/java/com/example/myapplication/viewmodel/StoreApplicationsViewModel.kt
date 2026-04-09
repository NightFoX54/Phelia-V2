package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.StoreApplication
import com.example.myapplication.data.repository.StoreApplicationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StoreApplicationsViewModel(
    private val repository: StoreApplicationRepository = StoreApplicationRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ViewModel() {

    private val _applications = MutableStateFlow<List<StoreApplication>>(emptyList())
    val applications: StateFlow<List<StoreApplication>> = _applications.asStateFlow()

    private val _listError = MutableStateFlow<String?>(null)
    val listError: StateFlow<String?> = _listError.asStateFlow()

    private var listener: ListenerRegistration? = null

    init {
        listener = repository.listenPendingApplications(
            onUpdate = { _applications.value = it },
            onError = { _listError.value = it },
        )
    }

    fun approve(applicationId: String, onResult: (Result<Unit>) -> Unit) {
        val adminUid = auth.currentUser?.uid.orEmpty()
        if (adminUid.isBlank()) {
            onResult(Result.failure(IllegalStateException("Not signed in")))
            return
        }
        viewModelScope.launch {
            onResult(repository.approveApplication(applicationId, adminUid))
        }
    }

    fun reject(applicationId: String, reason: String, onResult: (Result<Unit>) -> Unit) {
        val adminUid = auth.currentUser?.uid.orEmpty()
        if (adminUid.isBlank()) {
            onResult(Result.failure(IllegalStateException("Not signed in")))
            return
        }
        viewModelScope.launch {
            onResult(repository.rejectApplication(applicationId, adminUid, reason))
        }
    }

    override fun onCleared() {
        listener?.remove()
        super.onCleared()
    }
}
