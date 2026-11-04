package com.ejemplo.caminarcorrer.phone.sync

import com.ejemplo.caminarcorrer.phone.PhoneApplication
import com.ejemplo.caminarcorrer.shared.JsonConfig
import com.ejemplo.caminarcorrer.shared.model.ActivityPayload
import com.ejemplo.caminarcorrer.shared.sync.DataLayerPaths
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PhoneDataLayerService : WearableListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        val container = (application as PhoneApplication).container
        try {
            for (event in dataEvents) {
                if (event.type != DataEvent.TYPE_CHANGED) continue
                val path = event.dataItem.uri.path ?: continue
                if (!path.startsWith(DataLayerPaths.ACTIVITY_PREFIX)) continue

                val bytes = event.dataItem.data ?: continue
                val payload = runCatching {
                    JsonConfig.json.decodeFromString(
                        ActivityPayload.serializer(),
                        String(bytes, Charsets.UTF_8)
                    )
                }.getOrNull() ?: continue

                serviceScope.launch {
                    container.activityRepository.upsert(payload.activity)
                    container.syncRepository.sendAck(payload.activity.id)
                }
            }
        } finally {
            dataEvents.release()
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path != DataLayerPaths.REQUEST_PROFILE) return
        val container = (application as PhoneApplication).container
        serviceScope.launch {
            container.syncRepository.pushProfile()
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
