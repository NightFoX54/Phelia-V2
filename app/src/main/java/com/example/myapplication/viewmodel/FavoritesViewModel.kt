package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.FavoriteEntry
import com.example.myapplication.data.repository.FavoritesRepository
import com.example.myapplication.data.repository.ProductStatsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val repository: FavoritesRepository = FavoritesRepository(),
    private val productStatsRepository: ProductStatsRepository = ProductStatsRepository(),
) : ViewModel() {

    private val _favoriteEntries = MutableStateFlow<List<FavoriteEntry>>(emptyList())
    val favoriteEntries: StateFlow<List<FavoriteEntry>> = _favoriteEntries.asStateFlow()

    val favoriteProductIds: StateFlow<Set<String>> = _favoriteEntries
        .map { entries -> entries.map { it.productId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private var favoritesListener: ListenerRegistration? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { user ->
        favoritesListener?.remove()
        favoritesListener = null
        val uid = user?.uid
        if (uid.isNullOrBlank()) {
            _favoriteEntries.value = emptyList()
        } else {
            favoritesListener = repository.listenFavorites(uid) { list ->
                _favoriteEntries.value = list
            }
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authStateListener)
        favoritesListener?.remove()
        super.onCleared()
    }

    fun toggleFavorite(productId: String) {
        val uid = auth.currentUser?.uid ?: return
        if (productId.isBlank()) return
        viewModelScope.launch {
            repository.toggleFavorite(uid, productId).fold(
                onSuccess = { nowFavorited ->
                    val delta = if (nowFavorited) 1 else -1
                    productStatsRepository.adjustFavoriteCount(productId, delta)
                },
                onFailure = { },
            )
        }
    }
}
