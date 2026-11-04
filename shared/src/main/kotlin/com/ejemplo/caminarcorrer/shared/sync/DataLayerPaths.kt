package com.ejemplo.caminarcorrer.shared.sync

object DataLayerPaths {
    const val PROFILE = "/profile"
    const val ACTIVITY_PREFIX = "/activity/"
    const val ACK_PREFIX = "/ack/"
    const val REQUEST_SYNC = "/request-sync"
    const val REQUEST_PROFILE = "/request-profile"

    fun activity(activityId: String): String = "$ACTIVITY_PREFIX$activityId"

    fun ack(activityId: String): String = "$ACK_PREFIX$activityId"
}
