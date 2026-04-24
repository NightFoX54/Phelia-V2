package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.User
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

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _notifications = MutableStateFlow<List<UserNotificationItem>>(emptyList())
    val notifications: StateFlow<List<UserNotificationItem>> = _notifications.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private var notificationsListener: com.google.firebase.firestore.ListenerRegistration? = null

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

    fun loadUserProfile() {
        val uid = auth.currentUser?.uid.orEmpty()
        if (uid.isBlank()) return
        viewModelScope.launch {
            _loading.value = true
            repository.fetchUserProfile(uid).fold(
                onSuccess = { _userProfile.value = it },
                onFailure = { _message.value = it.message ?: "Could not load profile." }
            )
            _loading.value = false
        }
    }

    fun updateProfile(
        name: String,
        email: String,
        phone: String,
        bio: String,
        onEmailTaken: () -> Unit,
        onSuccess: () -> Unit,
        onSessionUpdate: (String, String) -> Unit = { _, _ -> }
    ) {
        val uid = auth.currentUser?.uid.orEmpty()
        if (uid.isBlank()) return
        viewModelScope.launch {
            _loading.value = true
            // 1. Check email availability
            val availableResult = repository.checkEmailAvailability(email, uid)
            val isAvailable = availableResult.getOrDefault(false)
            
            if (!isAvailable) {
                onEmailTaken()
                _loading.value = false
                return@launch
            }

            // 2. Update profile
            repository.updateUserProfile(uid, name, email, phone, bio).fold(
                onSuccess = {
                    _userProfile.value = _userProfile.value?.copy(name = name, email = email, phone = phone, bio = bio)
                    _message.value = "Profile updated successfully."
                    onSessionUpdate(name, email)
                    onSuccess()
                },
                onFailure = { _message.value = it.message ?: "Could not update profile." }
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
        notificationsListener?.remove()
        _loading.value = true
        notificationsListener = repository.listenNotifications(uid) {
            _notifications.value = it
            _loading.value = false
        }
    }

    override fun onCleared() {
        notificationsListener?.remove()
        super.onCleared()
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

    fun markAllNotificationsRead() {
        val uid = auth.currentUser?.uid.orEmpty()
        if (uid.isBlank()) return
        viewModelScope.launch {
            repository.markAllNotificationsAsRead(uid)
            _notifications.value = _notifications.value.map { it.copy(isRead = true) }
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
