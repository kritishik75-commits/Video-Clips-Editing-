package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.model.ApiKeyItem
import com.example.data.model.SessionDevice
import com.example.data.repository.AuthRepository
import com.example.data.repository.UserProfile
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    val userProfile: StateFlow<UserProfile> = authRepository.userProfile
    val apiKeys: StateFlow<List<ApiKeyItem>> = authRepository.apiKeys
    val activeSessions: StateFlow<List<SessionDevice>> = authRepository.activeSessions

    fun toggleRole() {
        authRepository.toggleRole()
    }

    fun toggleMfa() {
        authRepository.toggleMfa()
    }

    fun updateVaultPin(newPin: String) {
        authRepository.updateVaultPin(newPin)
    }

    fun createApiKey(name: String, rateLimitRps: Int) {
        authRepository.createApiKey(name, rateLimitRps)
    }

    fun revokeApiKey(id: String) {
        authRepository.revokeApiKey(id)
    }

    fun revokeSession(id: String) {
        authRepository.revokeSession(id)
    }
}
