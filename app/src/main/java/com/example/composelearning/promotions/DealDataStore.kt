package com.example.composelearning.promotions

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "deal_settings")

/**
 * Persistence boundary for the deal's target end timestamp.
 *
 * The ViewModel depends on this interface rather than the concrete DataStore implementation, so
 * tests can substitute a lightweight in-memory fake without touching disk or needing a [Context].
 */
interface DealStore {
    /** Emits the persisted target end time (epoch millis), or null if nothing has been saved yet. */
    val targetEndTime: Flow<Long?>

    /** Persists the target end time (epoch millis) so it survives process death. */
    suspend fun saveTargetEndTime(timestamp: Long)
}

class DealDataStore(private val context: Context) : DealStore {
    companion object {
        private val TARGET_END_TIME_KEY = longPreferencesKey("target_end_time")
    }

    override val targetEndTime: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[TARGET_END_TIME_KEY]
        }

    override suspend fun saveTargetEndTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[TARGET_END_TIME_KEY] = timestamp
        }
    }
}
