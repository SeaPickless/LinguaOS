package com.linguaos.app.ui.gamification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguaos.app.data.datastore.SessionDataStore
import com.linguaos.app.data.db.dao.AchievementDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val achievementDao: AchievementDao
) : ViewModel() {

    private val _earnedIds = MutableStateFlow<Set<String>>(emptySet())
    val earnedIds: StateFlow<Set<String>> = _earnedIds.asStateFlow()

    init {
        viewModelScope.launch {
            val uid = sessionDataStore.loggedInUserIdFlow.firstOrNull() ?: return@launch
            achievementDao.getAchievementsFlow(uid).collect { list ->
                _earnedIds.value = list.map { it.id }.toSet()
            }
        }
    }
}
