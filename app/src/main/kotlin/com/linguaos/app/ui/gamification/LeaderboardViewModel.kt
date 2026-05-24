package com.linguaos.app.ui.gamification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguaos.app.data.datastore.SessionDataStore
import com.linguaos.app.data.db.entity.UserEntity
import com.linguaos.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaderboardUiState(
    val users: List<UserEntity> = emptyList(),
    val currentUserId: Long = 0L,
    val isLoading: Boolean = true
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LeaderboardUiState())
    val state: StateFlow<LeaderboardUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val uid = sessionDataStore.loggedInUserIdFlow.firstOrNull() ?: 0L
            userRepository.getAllUsersFlow().collect { users ->
                _state.update { it.copy(users = users.sortedByDescending { u -> u.totalXp }, currentUserId = uid, isLoading = false) }
            }
        }
    }
}
