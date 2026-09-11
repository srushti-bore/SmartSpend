package com.smartspend.app.domain.model

sealed class AuthState {
    data class Unlocked(val profile: Profile) : AuthState()
    data class Locked(val profile: Profile) : AuthState()
    object NoProfile : AuthState()
}
