package com.ejemplo.caminarcorrer.phone.sync

import android.content.Context
import com.ejemplo.caminarcorrer.phone.data.PhoneProfileRepository
import com.ejemplo.caminarcorrer.shared.JsonConfig
import com.ejemplo.caminarcorrer.shared.model.AckPayload
import com.ejemplo.caminarcorrer.shared.model.ProfilePayload
import com.ejemplo.caminarcorrer.shared.sync.DataLayerPaths
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

class PhoneSyncRepository(
    private val context: Context,
    private val profileRepository: PhoneProfileRepository
) {

    val isAvailable: Boolean
        get() = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

    suspend fun pushProfile(): Boolean {
        val profile = profileRepository.profile.first() ?: return false
        val payload = ProfilePayload(
            weightKg = profile.weightKg,
            updatedAtEpochMillis = profile.updatedAtEpochMillis
        )
        return putDataItem(
            DataLayerPaths.PROFILE,
            JsonConfig.json.encodeToString(ProfilePayload.serializer(), payload)
                .toByteArray(Charsets.UTF_8)
        )
    }

    suspend fun sendAck(activityId: String): Boolean {
        val payload = AckPayload(
            activityId = activityId,
            receivedAtEpochMillis = System.currentTimeMillis()
        )
        return putDataItem(
            DataLayerPaths.ack(activityId),
            JsonConfig.json.encodeToString(AckPayload.serializer(), payload)
                .toByteArray(Charsets.UTF_8)
        )
    }

    suspend fun requestSync(): Boolean {
        if (!isAvailable) return false
        val node = findNode() ?: return false
        return runCatching {
            Wearable.getMessageClient(context)
                .sendMessage(node.id, DataLayerPaths.REQUEST_SYNC, ByteArray(0))
                .await()
            true
        }.getOrDefault(false)
    }

    private suspend fun findNode(): Node? = runCatching {
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
