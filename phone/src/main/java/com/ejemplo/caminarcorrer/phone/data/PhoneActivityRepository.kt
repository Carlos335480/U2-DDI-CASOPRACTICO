package com.ejemplo.caminarcorrer.phone.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.ejemplo.caminarcorrer.shared.JsonConfig
import com.ejemplo.caminarcorrer.shared.model.ActivityRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer

class PhoneActivityRepository(private val context: Context) {

    val activities: Flow<List<ActivityRecord>> = context.phoneDataStore.data.map { preferences ->
        decodeActivities(preferences[PhoneKeys.ACTIVITIES])
    }

    suspend fun upsert(activity: ActivityRecord) {
        context.phoneDataStore.edit { preferences ->
            val current = decodeActivities(preferences[PhoneKeys.ACTIVITIES])
            val normalized = activity.copy(synced = true)
            val updated = (current.filterNot { it.id == normalized.id } + normalized)
                .sortedByDescending { it.startEpochMillis }
            preferences[PhoneKeys.ACTIVITIES] = encodeActivities(updated)
        }
    }

    private fun decodeActivities(json: String?): List<ActivityRecord> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            JsonConfig.json.decodeFromString(
                ListSerializer(ActivityRecord.serializer()),
                json
            )
        }.getOrDefault(emptyList())
    }

    private fun encodeActivities(activities: List<ActivityRecord>): String =
        JsonConfig.json.encodeToString(
            ListSerializer(ActivityRecord.serializer()),
            activities
        )
}
