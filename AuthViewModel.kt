package com.gameboost.pro.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        _authState.value = _authState.value.copy(
            isSignedIn = authRepository.isUserSignedIn(),
            isLoading = false
        )
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)
            
            val result = authRepository.signInWithGoogle(idToken)
            
            _authState.value = if (result.isSuccess) {
                _authState.value.copy(
                    isSignedIn = true,
                    isLoading = false,
                    errorMessage = null,
                    isGuestMode = false
                )
            } else {
                _authState.value.copy(
                    isSignedIn = false,
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun continueAsGuest() {
        _authState.value = _authState.value.copy(
            isSignedIn = true,
            isGuestMode = true,
            isLoading = false,
            errorMessage = null
        )
    }

    fun signOut() {
        authRepository.signOut()
        _authState.value = _authState.value.copy(
            isSignedIn = false,
            isGuestMode = false,
            errorMessage = null
        )
    }

    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }
}

data class AuthState(
    val isSignedIn: Boolean = false,
    val isGuestMode: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
