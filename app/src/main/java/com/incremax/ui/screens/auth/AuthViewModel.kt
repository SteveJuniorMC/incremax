package com.incremax.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.incremax.domain.model.AuthResult
import com.incremax.domain.model.AuthState
import com.incremax.domain.repository.AuthRepository
import com.incremax.domain.repository.SyncRepository
import com.incremax.domain.repository.SyncStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val authState: AuthState = AuthState.Loading,
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val showLocalDataWarning: Boolean = false,
    val emailInput: String = "",
    val passwordInput: String = "",
    val isSignUpMode: Boolean = false,
    val signInComplete: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.authState.collect { state ->
                _uiState.update { it.copy(authState = state) }

                if (state is AuthState.Authenticated) {
                    checkDataConflict(state.user.uid)
                }
            }
        }

        viewModelScope.launch {
            syncRepository.syncStatus.collect { status ->
                _uiState.update { it.copy(syncStatus = status) }
            }
        }
    }

    private suspend fun checkDataConflict(userId: String) {
        val hasCloudData = syncRepository.hasCloudData(userId)
        val hasLocalData = syncRepository.hasLocalData()

        if (hasCloudData && hasLocalData) {
            _uiState.update { it.copy(showLocalDataWarning = true, isLoading = false) }
        } else {
            performInitialSync(userId)
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is AuthResult.Success -> {
                    // Auth state listener will handle the rest
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun signInWithEmail() {
        val email = _uiState.value.emailInput.trim()
        val password = _uiState.value.passwordInput

        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email") }
            return
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your password") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = if (_uiState.value.isSignUpMode) {
                authRepository.signUpWithEmail(email, password)
            } else {
                authRepository.signInWithEmail(email, password)
            }

            when (result) {
                is AuthResult.Success -> {
                    // Auth state listener handles the rest
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun sendPasswordResetEmail() {
        val email = _uiState.value.emailInput.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email first") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.sendPasswordResetEmail(email)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Password reset email sent to $email"
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun confirmUseCloudData() {
        viewModelScope.launch {
            val state = _uiState.value.authState
            if (state is AuthState.Authenticated) {
                _uiState.update { it.copy(showLocalDataWarning = false, isLoading = true) }
                syncRepository.replaceLocalWithCloud(state.user.uid)
                _uiState.update { it.copy(isLoading = false, signInComplete = true) }
            }
        }
    }

    fun confirmKeepLocalData() {
        viewModelScope.launch {
            val state = _uiState.value.authState
            if (state is AuthState.Authenticated) {
                _uiState.update { it.copy(showLocalDataWarning = false, isLoading = true) }
                syncRepository.uploadLocalToCloud(state.user.uid)
                _uiState.update { it.copy(isLoading = false, signInComplete = true) }
            }
        }
    }

    private suspend fun performInitialSync(userId: String) {
        _uiState.update { it.copy(isLoading = true) }
        syncRepository.performInitialSync(userId)
        _uiState.update { it.copy(isLoading = false, signInComplete = true) }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(emailInput = email, error = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(passwordInput = password, error = null) }
    }

    fun toggleSignUpMode() {
        _uiState.update { it.copy(isSignUpMode = !it.isSignUpMode, error = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.update { it.copy(signInComplete = false) }
        }
    }

    fun skipSignIn() {
        _uiState.update { it.copy(signInComplete = true) }
    }
}
