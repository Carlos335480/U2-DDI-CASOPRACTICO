package com.ejemplo.caminarcorrer.wear.sync

import com.ejemplo.caminarcorrer.shared.JsonConfig
import com.ejemplo.caminarcorrer.shared.model.AckPayload
import com.ejemplo.caminarcorrer.shared.model.ProfilePayload
import com.ejemplo.caminarcorrer.shared.sync.DataLayerPaths
import com.ejemplo.caminarcorrer.wear.WearApplication
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class WatchDataLayerService : WearableListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        val container = (application as WearApplication).container
        try {
            for (event in dataEvents) {
                if (event.type != DataEvent.TYPE_CHANGED) continue
                val path = event.dataItem.uri.path ?: continue
                val bytes = event.dataItem.data ?: continue
                val json = String(bytes, Charsets.UTF_8)

                when {
                    path == DataLayerPaths.PROFILE -> {
                        val payload = runCatching {
                            JsonConfig.json.decodeFromString(ProfilePayload.serializer(), json)
                        }.getOrNull() ?: continue
                        serviceScope.launch {
                            container.profileRepository.saveIfNewer(payload)
                        }
                    }

                    path.startsWith(DataLayerPaths.ACK_PREFIX) -> {
                        val payload = runCatching {
                            JsonConfig.json.decodeFromString(AckPayload.serializer(), json)
                        }.getOrNull() ?: continue
                        serviceScope.launch {
                            container.activityRepository.markSynced(payload.activityId)
                        }
                    }
                }
            }
        } finally {
            dataEvents.release()
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path != DataLayerPaths.REQUEST_SYNC) return
        val container = (application as WearApplication).container
        serviceScope.launch {
            container.syncRepository.pushPendingActivities()
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
