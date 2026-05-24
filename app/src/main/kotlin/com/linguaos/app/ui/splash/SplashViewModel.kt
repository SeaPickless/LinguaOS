package com.linguaos.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguaos.app.data.datastore.SessionDataStore
import com.linguaos.app.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val userId    = sessionDataStore.loggedInUserIdFlow.firstOrNull()
            val onboarded = sessionDataStore.onboardingDoneFlow.firstOrNull() ?: false

            _startDestination.value = when {
                userId != null && onboarded -> Routes.DASHBOARD
                onboarded                  -> Routes.LOGIN
                else                       -> Routes.ONBOARDING
            }
        }
    }
}
