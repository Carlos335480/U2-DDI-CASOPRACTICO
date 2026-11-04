package com.ejemplo.caminarcorrer.phone

import android.content.Context
import com.ejemplo.caminarcorrer.phone.data.PhoneActivityRepository
import com.ejemplo.caminarcorrer.phone.data.PhoneProfileRepository
import com.ejemplo.caminarcorrer.phone.sync.PhoneSyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PhoneAppContainer(context: Context) {

    private val appContext = context.applicationContext
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val profileRepository = PhoneProfileRepository(appContext)
    val activityRepository = PhoneActivityRepository(appContext)
    val syncRepository = PhoneSyncRepository(appContext, profileRepository)

    init {
        appScope.launch {
            syncRepository.pushProfile()
        }
    }
}
