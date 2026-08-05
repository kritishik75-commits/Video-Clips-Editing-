package com.example.data.repository

import com.example.data.model.ApiKeyItem
import com.example.data.model.SessionDevice
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class UserProfile(
    val email: String = "kritishik75@gmail.com",
    val displayName: String = "Enterprise Administrator",
    val role: UserRole = UserRole.ADMIN,
    val avatarUrl: String = "",
    val subscriptionTier: String = "Enterprise Unlimited",
    val storageLimitGb: Int = 100,
    val isMfaEnabled: Boolean = true,
    val isVaultPasscodeSet: Boolean = true,
    val vaultPin: String = "1234"
)

class AuthRepository {
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _apiKeys = MutableStateFlow(
        listOf(
            ApiKeyItem(
                id = "k1",
                name = "Production CDN Sync Key",
                keyPrefix = "ak_live_98a72...",
                createdDate = "2026-06-12",
                rateLimitRps = 250,
                isActive = true
            ),
            ApiKeyItem(
                id = "k2",
                name = "Backup Pipeline Secret",
                keyPrefix = "ak_test_10d84...",
                createdDate = "2026-07-28",
                rateLimitRps = 50,
                isActive = true
            )
        )
    )
    val apiKeys: StateFlow<List<ApiKeyItem>> = _apiKeys.asStateFlow()

    private val _activeSessions = MutableStateFlow(
        listOf(
            SessionDevice(
                id = "s1",
                name = "Google Pixel 9 Pro (This Device)",
                platform = "Android 16 / Compose",
                ipAddress = "192.168.1.105",
                location = "San Francisco, CA",
                lastActive = "Active Now",
                isCurrentDevice = true
            ),
            SessionDevice(
                id = "s2",
                name = "MacBook Pro M3 Max",
                platform = "macOS 15.1 / Chrome",
                ipAddress = "192.168.1.140",
                location = "San Francisco, CA",
                lastActive = "2 hours ago"
            ),
            SessionDevice(
                id = "s3",
                name = "Cloud Node Worker #4",
                platform = "Ubuntu 24.04 / Node.js",
                ipAddress = "34.120.95.12",
                location = "us-west1 (Oregon)",
                lastActive = "1 day ago"
            )
        )
    )
    val activeSessions: StateFlow<List<SessionDevice>> = _activeSessions.asStateFlow()

    fun toggleRole() {
        val current = _userProfile.value
        val nextRole = if (current.role == UserRole.ADMIN) UserRole.PRO_USER else UserRole.ADMIN
        _userProfile.value = current.copy(role = nextRole)
    }

    fun toggleMfa() {
        val current = _userProfile.value
        _userProfile.value = current.copy(isMfaEnabled = !current.isMfaEnabled)
    }

    fun verifyVaultPin(enteredPin: String): Boolean {
        return enteredPin == _userProfile.value.vaultPin
    }

    fun updateVaultPin(newPin: String) {
        _userProfile.value = _userProfile.value.copy(vaultPin = newPin, isVaultPasscodeSet = true)
    }

    fun createApiKey(name: String, rateLimitRps: Int): ApiKeyItem {
        val key = ApiKeyItem(
            id = UUID.randomUUID().toString().take(8),
            name = name,
            keyPrefix = "ak_live_" + UUID.randomUUID().toString().take(6) + "...",
            createdDate = "2026-08-05",
            rateLimitRps = rateLimitRps,
            isActive = true
        )
        _apiKeys.value = _apiKeys.value + key
        return key
    }

    fun revokeApiKey(id: String) {
        _apiKeys.value = _apiKeys.value.filter { it.id != id }
    }

    fun revokeSession(id: String) {
        _activeSessions.value = _activeSessions.value.filter { it.id != id }
    }
}
