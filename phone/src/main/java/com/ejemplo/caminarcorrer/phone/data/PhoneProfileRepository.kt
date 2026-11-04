package com.ejemplo.caminarcorrer.phone.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.ejemplo.caminarcorrer.shared.JsonConfig
import com.ejemplo.caminarcorrer.shared.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PhoneProfileRepository(private val context: Context) {

    val profile: Flow<UserProfile?> = context.phoneDataStore.data.map { preferences ->
        preferences[PhoneKeys.PROFILE]?.let { json ->
            runCatching {
                JsonConfig.json.decodeFromString(UserProfile.serializer(), json)
            }.getOrNull()
        }
    }

    suspend fun save(profile: UserProfile) {
        context.phoneDataStore.edit { preferences ->
            preferences[PhoneKeys.PROFILE] =
                JsonConfig.json.encodeToString(UserProfile.serializer(), profile)
        }
    }
}
