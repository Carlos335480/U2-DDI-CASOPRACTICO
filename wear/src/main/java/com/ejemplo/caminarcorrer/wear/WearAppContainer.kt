package com.ejemplo.caminarcorrer.wear

import android.content.Context
import com.ejemplo.caminarcorrer.shared.model.ActivityRecord
import com.ejemplo.caminarcorrer.wear.data.WatchActivityRepository
import com.ejemplo.caminarcorrer.wear.data.WatchProfileRepository
import com.ejemplo.caminarcorrer.wear.sync.WatchSyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WearAppContainer(context: Context) {

    private val appContext = context.applicationContext
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val profileRepository = WatchProfileRepository(appContext)
    val activityRepository = WatchActivityRepository(appContext)
    val syncRepository = WatchSyncRepository(
        context = appContext,
        profileRepository = profileRepository,
        activityRepository = activityRepository
    )

    init {
        appScope.launch {
            syncRepository.refreshProfile()
            syncRepository.requestProfile()
            syncRepository.pushPendingActivities()
        }
    }

    fun persistAndSync(activity: ActivityRecord) {
        appScope.launch {
            activityRepository.add(activity)
            syncRepository.pushActivity(activity)
        }
    }
}
