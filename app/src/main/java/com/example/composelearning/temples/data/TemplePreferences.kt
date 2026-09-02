package com.example.composelearning.temples.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.templeDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "temple_showcase")

/**
 * The only mutable state in the feature: which temples the user has saved, and which they
 * have ticked off as visited. Everything else is static data.
 *
 * Persisted with DataStore so a saved list survives process death — a pilgrimage plan the
 * user builds on the bus should still be there when they arrive.
 */
class TemplePreferences(context: Context) {

    private val store = context.templeDataStore

    val favourites: Flow<Set<String>> = store.data.map { it[FAVOURITES] ?: emptySet() }

    val visited: Flow<Set<String>> = store.data.map { it[VISITED] ?: emptySet() }

    suspend fun toggleFavourite(templeId: String) = toggle(FAVOURITES, templeId)

    suspend fun toggleVisited(templeId: String) = toggle(VISITED, templeId)

    private suspend fun toggle(key: Preferences.Key<Set<String>>, id: String) {
        store.edit { prefs ->
            val existing = prefs[key] ?: emptySet()
            // Copy rather than mutate: the set handed to us by DataStore is not ours to change.
            prefs[key] = if (id in existing) existing - id else existing + id
        }
    }

    private companion object {
        val FAVOURITES = stringSetPreferencesKey("favourite_temple_ids")
        val VISITED = stringSetPreferencesKey("visited_temple_ids")
    }
}
