package com.example.appdelivery.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionStore by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {
    private val userIdKey = longPreferencesKey("user_id")
    private val courierIdKey = longPreferencesKey("courier_id")
    private val roleKey = stringPreferencesKey("user_role") // "CUSTOMER" o "COURIER"

    val userId: Flow<Long?> = context.sessionStore.data.map { it[userIdKey] }
    val courierId: Flow<Long?> = context.sessionStore.data.map { it[courierIdKey] }
    val role: Flow<String?> = context.sessionStore.data.map { it[roleKey] }

    suspend fun save(id: Long) {
        context.sessionStore.edit {
            it[userIdKey] = id
            it[roleKey] = "CUSTOMER"
            it.remove(courierIdKey)
        }
    }

    suspend fun saveCourier(id: Long) {
        context.sessionStore.edit {
            it[courierIdKey] = id
            it[roleKey] = "COURIER"
            it.remove(userIdKey)
        }
    }

    suspend fun clear() {
        context.sessionStore.edit {
            it.remove(userIdKey)
            it.remove(courierIdKey)
            it.remove(roleKey)
        }
    }
}
