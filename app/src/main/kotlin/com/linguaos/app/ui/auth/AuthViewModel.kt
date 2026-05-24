package com.linguaos.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguaos.app.data.db.entity.UserEntity
import com.linguaos.app.data.repository.AuthResult
import com.linguaos.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val allUsers: List<UserEntity> = emptyList()
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.getAllUsersFlow().collect { users ->
                _state.update { it.copy(allUsers = users) }
            }
        }
    }

    fun updateUsername(v: String) = _state.update { it.copy(username = v, error = null) }
    fun updatePassword(v: String) = _state.update { it.copy(password = v, error = null) }

    fun login(onSuccess: () -> Unit) {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val r = userRepository.login(s.username, s.password)) {
                is AuthResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = r.message) }
                }
            }
        }
    }

    /** Switch to an existing local profile without needing a password (same device). */
    fun loginAs(userId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val ok = userRepository.loginAs(userId)
            _state.update { it.copy(isLoading = false) }
            if (ok) onSuccess()
        }
    }

    fun resetPassword(username: String, newPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val r = userRepository.resetPassword(username, newPassword)) {
                is AuthResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = r.message) }
                }
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            userRepository.logout()
            onDone()
        }
    }
}
