package com.ejemplo.caminarcorrer.wear.sync

import android.content.Context
import com.ejemplo.caminarcorrer.shared.JsonConfig
import com.ejemplo.caminarcorrer.shared.model.ActivityPayload
import com.ejemplo.caminarcorrer.shared.model.ProfilePayload
import com.ejemplo.caminarcorrer.shared.sync.DataLayerPaths
import com.ejemplo.caminarcorrer.wear.data.WatchActivityRepository
import com.ejemplo.caminarcorrer.wear.data.WatchProfileRepository
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WatchSyncRepository(
    private val context: Context,
    private val profileRepository: WatchProfileRepository,
    private val activityRepository: WatchActivityRepository
) {

    val isAvailable: Boolean
        get() = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

    suspend fun pushPendingActivities(): Int {
        if (!isAvailable) return 0
        val pending = activityRepository.pending()
        var sent = 0
        for (activity in pending) {
            if (pushActivity(activity)) sent++
        }
        return sent
    }

    suspend fun pushActivity(activity: com.ejemplo.caminarcorrer.shared.model.ActivityRecord): Boolean {
        val payload = ActivityPayload(activity = activity)
        return putDataItem(
            DataLayerPaths.activity(activity.id),
            JsonConfig.json.encodeToString(ActivityPayload.serializer(), payload)
                .toByteArray(Charsets.UTF_8)
        )
    }

    suspend fun refreshProfile(): Boolean {
        if (!isAvailable) return false
        val bytes = readDataItem(DataLayerPaths.PROFILE) ?: return false
        val payload = runCatching {
            JsonConfig.json.decodeFromString(
                ProfilePayload.serializer(),
                String(bytes, Charsets.UTF_8)
            )
        }.getOrNull() ?: return false
        profileRepository.saveIfNewer(payload)
        return true
    }

    suspend fun requestProfile(): Boolean {
        if (!isAvailable) return false
        val node = findPhoneNode() ?: return false
        return runCatching {
            Wearable.getMessageClient(context)
                .sendMessage(node.id, DataLayerPaths.REQUEST_PROFILE, ByteArray(0))
                .await()
            true
        }.getOrDefault(false)
    }

    private suspend fun readDataItem(path: String): ByteArray? = runCatching {
        val buffer = Wearable.getDataClient(context).getDataItems().await()
        try {
            buffer.firstOrNull { it.uri.path == path }?.data
        } finally {
            buffer.release()
        }
    }.getOrNull()

    private suspend fun findPhoneNode(): Node? = runCatching {
        val nodes = Wearable.getNodeClient(context).connectedNodes.await()
        nodes.firstOrNull { it.isNearby } ?: nodes.firstOrNull()
    }.getOrNull()

    private suspend fun putDataItem(path: String, bytes: ByteArray): Boolean {
        if (!isAvailable) return false
        return runCatching {
            Wearable.getDataClient(context)
                .putDataItem(
                    PutDataRequest.create(path)
                        .setData(bytes)
                        .setUrgent()
                )
                .await()
            true
        }.getOrDefault(false)
    }
}
