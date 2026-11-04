package com.ejemplo.caminarcorrer.wear.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.ejemplo.caminarcorrer.shared.JsonConfig
import com.ejemplo.caminarcorrer.shared.model.ProfilePayload
import com.ejemplo.caminarcorrer.shared.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class WatchProfileRepository(private val context: Context) {

    val profile: Flow<UserProfile?> = context.wearDataStore.data.map { preferences ->
        preferences[WearKeys.PROFILE]?.let { json ->
            runCatching {
                JsonConfig.json.decodeFromString(UserProfile.serializer(), json)
            }.getOrNull()
        }
    }

    suspend fun current(): UserProfile? = profile.first()

    suspend fun save(profile: UserProfile) {
        context.wearDataStore.edit { preferences ->
            preferences[WearKeys.PROFILE] =
                JsonConfig.json.encodeToString(UserProfile.serializer(), profile)
        }
    }

    suspend fun saveIfNewer(payload: ProfilePayload) {
        val current = current()
        if (current == null || payload.updatedAtEpochMillis >= current.updatedAtEpochMillis) {
            save(
                UserProfile(
                    weightKg = payload.weightKg,
                    updatedAtEpochMillis = payload.updatedAtEpochMillis
                )
            )
        }
    }
}
