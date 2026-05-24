package com.linguaos.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val store = context.sessionDataStore

    companion object {
        val KEY_LOGGED_IN_USER_ID  = longPreferencesKey("logged_in_user_id")
        val KEY_LAST_USERNAME      = stringPreferencesKey("last_username")
        val KEY_ONBOARDING_DONE    = booleanPreferencesKey("onboarding_done")
        val KEY_DB_SEEDED          = booleanPreferencesKey("db_seeded")
    }

    val loggedInUserIdFlow: Flow<Long?> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_LOGGED_IN_USER_ID] }

    val lastUsernameFlow: Flow<String?> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_LAST_USERNAME] }

    val onboardingDoneFlow: Flow<Boolean> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_ONBOARDING_DONE] ?: false }

    val dbSeededFlow: Flow<Boolean> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[KEY_DB_SEEDED] ?: false }

    suspend fun setLoggedInUser(userId: Long, username: String) {
        store.edit { prefs ->
            prefs[KEY_LOGGED_IN_USER_ID] = userId
            prefs[KEY_LAST_USERNAME] = username
        }
    }

    suspend fun clearSession() {
        store.edit { it.remove(KEY_LOGGED_IN_USER_ID) }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        store.edit { it[KEY_ONBOARDING_DONE] = done }
    }

    suspend fun setDbSeeded(seeded: Boolean) {
        store.edit { it[KEY_DB_SEEDED] = seeded }
    }
}
