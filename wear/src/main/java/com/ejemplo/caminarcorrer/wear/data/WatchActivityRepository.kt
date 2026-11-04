package com.ejemplo.caminarcorrer.wear.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.ejemplo.caminarcorrer.shared.JsonConfig
import com.ejemplo.caminarcorrer.shared.model.ActivityRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer

class WatchActivityRepository(private val context: Context) {

    val activities: Flow<List<ActivityRecord>> = context.wearDataStore.data.map { preferences ->
        decodeActivities(preferences[WearKeys.ACTIVITIES])
    }

    suspend fun add(activity: ActivityRecord) {
        context.wearDataStore.edit { preferences ->
            val current = decodeActivities(preferences[WearKeys.ACTIVITIES])
            val updated = (current.filterNot { it.id == activity.id } + activity)
                .sortedByDescending { it.startEpochMillis }
            preferences[WearKeys.ACTIVITIES] = encodeActivities(updated)
        }
    }

    suspend fun markSynced(activityId: String) {
        context.wearDataStore.edit { preferences ->
            val current = decodeActivities(preferences[WearKeys.ACTIVITIES])
            val updated = current.map { activity ->
                if (activity.id == activityId) activity.copy(synced = true) else activity
            }
            preferences[WearKeys.ACTIVITIES] = encodeActivities(updated)
        }
    }

    suspend fun pending(): List<ActivityRecord> =
        activities.first().filter { !it.synced }

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
