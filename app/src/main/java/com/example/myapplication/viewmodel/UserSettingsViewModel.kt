package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.UserNotificationItem
import com.example.myapplication.data.repository.UserSettings
import com.example.myapplication.data.repository.UserSettingsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserSettingsViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val repository: UserSettingsRepository = UserSettingsRepository(),
) : ViewModel() {
    private val _settings = MutableStateFlow(UserSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private val _notifications = MutableStateFlow<List<UserNotificationItem>>(emptyList())
    val notifications: StateFlow<List<UserNotificationItem>> = _notifications.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun loadSettings() {
        val uid = auth.currentUser?.uid.orEmpty()
        if (uid.isBlank()) return
        viewModelScope.launch {
            _loading.value = true
            repository.fetchSettings(uid).fold(
                onSuccess = { _settings.value = it },
                onFailure = { _message.value = it.message ?: "Could not load settings." },
            )
            _loading.value = false
        }
    }

    fun updateSettings(settings: UserSettings) {
        _settings.value = settings
    }

    fun saveSettings() {
        val uid = auth.currentUser?.uid.orEmpty()
        if (uid.isBlank()) return
        viewModelScope.launch {
            _loading.value = true
            repository.saveSettings(uid, _settings.value).fold(
                onSuccess = { _message.value = "Settings saved." },
                onFailure = { _message.value = it.message ?: "Could not save settings." },
            )
            _loading.value = false
        }
    }

    fun loadNotifications() {
        val uid = auth.currentUser?.uid.orEmpty()
        if (uid.isBlank()) return
        viewModelScope.launch {
            _loading.value = true
            repository.fetchNotifications(uid).fold(
                onSuccess = { _notifications.value = it },
                onFailure = { _message.value = it.message ?: "Could not load notifications." },
            )
            _loading.value = false
        }
    }

    fun markNotificationRead(notificationId: String) {
        val uid = auth.currentUser?.uid.orEmpty()
        if (uid.isBlank()) return
        viewModelScope.launch {
            repository.markNotificationRead(uid, notificationId)
            _notifications.value = _notifications.value.map { n ->
                if (n.id == notificationId) n.copy(isRead = true) else n
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        val uid = auth.currentUser?.uid.orEmpty()
        if (uid.isBlank()) return
        viewModelScope.launch {
            repository.deleteNotification(uid, notificationId).fold(
                onSuccess = {
                    _notifications.value = _notifications.value.filterNot { it.id == notificationId }
                },
                onFailure = {
                    _message.value = it.message ?: "Could not delete notification."
                },
            )
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
