package com.example.appdelivery.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionStore by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {
    private val userIdKey = longPreferencesKey("user_id")

    val userId: Flow<Long?> = context.sessionStore.data.map { it[userIdKey] }

    suspend fun save(id: Long) {
        context.sessionStore.edit { it[userIdKey] = id }
    }

    suspend fun clear() {
        context.sessionStore.edit { it.remove(userIdKey) }
    }
}
