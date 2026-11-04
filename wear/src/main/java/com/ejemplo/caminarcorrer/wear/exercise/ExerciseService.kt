@file:android.annotation.SuppressLint("RestrictedApi", "WrongConstant")

package com.ejemplo.caminarcorrer.wear.exercise

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseState
import androidx.health.services.client.data.ExerciseTrackedStatus
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.LocationAvailability
import androidx.health.services.client.endExercise
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.getCurrentExerciseInfo
import androidx.health.services.client.pauseExercise
import androidx.health.services.client.resumeExercise
import androidx.health.services.client.startExercise
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.ejemplo.caminarcorrer.shared.logic.CalorieCalculator
import com.ejemplo.caminarcorrer.shared.logic.MetricsCalculator
import com.ejemplo.caminarcorrer.shared.model.ActivityRecord
import com.ejemplo.caminarcorrer.shared.model.ActivityType
import com.ejemplo.caminarcorrer.shared.model.DistanceSource
import com.ejemplo.caminarcorrer.wear.MainActivity
import com.ejemplo.caminarcorrer.wear.R
import com.ejemplo.caminarcorrer.wear.WearApplication
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ExerciseService : LifecycleService() {

    private val container: com.ejemplo.caminarcorrer.wear.WearAppContainer
        get() = (application as WearApplication).container

    private var exerciseClient: ExerciseClient? = null
    private var timerJob: Job? = null
    private var sessionStarted = false
    private var ending = false
    private var startEpochMillis = 0L
    private var startElapsedRealtime = 0L
    private var pausedElapsedRealtime = 0L
    private var totalPausedMillis = 0L
    private var weightKg: Double? = null
    private var gpsEnabled = false
    private var lastDistanceMeters: Double? = null
    private var lastCheckpoint: ExerciseUpdate.ActiveDurationCheckpoint? = null

    private val updateCallback = object : ExerciseUpdateCallback {
        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            update.activeDurationCheckpoint?.let { lastCheckpoint = it }
            update.latestMetrics.getData(DataType.DISTANCE_TOTAL)?.total?.let {
                lastDistanceMeters = it
            }

            val state = update.exerciseStateInfo.state
            when {
                state.isEnded -> finishSession()
                state.isPaused -> ExerciseSessionStore.update { it.copy(phase = ExercisePhase.PAUSED) }
                state == ExerciseState.ACTIVE ->
                    ExerciseSessionStore.update { it.copy(phase = ExercisePhase.ACTIVE) }
            }
            refreshMetrics()
        }

        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) = Unit

        override fun onAvailabilityChanged(
            dataType: DataType<*, *>,
            availability: Availability
        ) {
            if (availability is LocationAvailability) {
                // No guardamos ubicaciones. La disponibilidad solo afecta la nota de GPS.
            }
        }

        override fun onRegistered() = Unit

        override fun onRegistrationFailed(throwable: Throwable) {
            fail("No se pudo iniciar el seguimiento: ${throwable.message ?: "error desconocido"}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> {
                val type = intent.getStringExtra(EXTRA_TYPE)
                    ?.let { runCatching { ActivityType.valueOf(it) }.getOrNull() }
                    ?: ActivityType.WALKING
                gpsEnabled = intent.getBooleanExtra(EXTRA_GPS, false) &&
                    ExercisePermissions.canUseGps(this)
                startForegroundForSession(type, gpsEnabled)
                lifecycleScope.launch { startSession(type) }
            }

            ACTION_PAUSE -> lifecycleScope.launch {
                if (sessionStarted) pauseSession() else stopSelf()
            }

            ACTION_RESUME -> lifecycleScope.launch {
                if (sessionStarted) resumeSession() else stopSelf()
            }

            ACTION_END -> lifecycleScope.launch {
                if (sessionStarted) requestEnd() else stopSelf()
            }
            else -> if (!sessionStarted) stopSelf()
        }

        return START_STICKY
    }

    private suspend fun startSession(type: ActivityType) {
        if (sessionStarted) return
        sessionStarted = true
        startEpochMillis = System.currentTimeMillis()
        startElapsedRealtime = SystemClock.elapsedRealtime()
        totalPausedMillis = 0L
        pausedElapsedRealtime = 0L
        weightKg = container.profileRepository.current()?.weightKg

        ExerciseSessionStore.set(
            ExerciseSessionState(
                phase = ExercisePhase.STARTING,
                type = type,
                gpsEnabled = gpsEnabled,
                distanceSource = if (gpsEnabled) DistanceSource.GPS else DistanceSource.STEPS,
                weightKg = weightKg
            )
        )

        if (!ExercisePermissions.hasActivityRecognition(this)) {
            fail("Falta el permiso de actividad física. No se puede registrar la actividad.")
            return
        }

        try {
            val healthClient = HealthServices.getClient(this)
            val client = healthClient.exerciseClient
            exerciseClient = client

            val trackedStatus = client.getCurrentExerciseInfo().exerciseTrackedStatus
            if (trackedStatus == ExerciseTrackedStatus.OTHER_APP_IN_PROGRESS) {
                fail("Otra app está registrando una actividad. Ciérrala e inténtalo de nuevo.")
                return
            }
            if (trackedStatus == ExerciseTrackedStatus.OWNED_EXERCISE_IN_PROGRESS) {
                fail("Ya hay una actividad en curso en esta app.")
                return
            }

            val capabilities = client.getCapabilities()
            val exerciseType = when (type) {
                ActivityType.WALKING -> ExerciseType.WALKING
                ActivityType.RUNNING -> ExerciseType.RUNNING
            }

            if (exerciseType !in capabilities.supportedExerciseTypes) {
                startTimerOnly("Este reloj no soporta este tipo de actividad. Se registrará solo el tiempo.")
                return
            }

            val typeCapabilities = capabilities.getExerciseTypeCapabilities(exerciseType)
            if (DataType.DISTANCE_TOTAL !in typeCapabilities.supportedDataTypes) {
                startTimerOnly("Este reloj no reporta distancia. Se registrará solo el tiempo.")
                return
            }

            client.setUpdateCallback(updateCallback)

            val config = ExerciseConfig(
                exerciseType = exerciseType,
                dataTypes = setOf(DataType.DISTANCE_TOTAL),
                isAutoPauseAndResumeEnabled = false,
                isGpsEnabled = gpsEnabled,
                exerciseGoals = mutableListOf()
            )
            client.startExercise(config)

            ExerciseSessionStore.update {
                it.copy(
                    phase = ExercisePhase.ACTIVE,
                    type = type,
                    gpsEnabled = gpsEnabled,
                    distanceSource = if (gpsEnabled) DistanceSource.GPS else DistanceSource.STEPS
                )
            }
            startTimer()
        } catch (exception: Exception) {
            startTimerOnly(
                "Health Services no está disponible. Se registrará solo el tiempo."
            )
        }
    }

    private suspend fun startTimerOnly(notice: String) {
        runCatching { exerciseClient?.endExercise() }
        runCatching { exerciseClient?.clearUpdateCallbackAsync(updateCallback) }
        exerciseClient = null
        gpsEnabled = false
        lastDistanceMeters = null
        lastCheckpoint = null

        ExerciseSessionStore.update {
            it.copy(
                phase = ExercisePhase.ACTIVE,
                mode = ExerciseMode.TIMER_ONLY,
                gpsEnabled = false,
                distanceSource = DistanceSource.NONE,
                errorMessage = null,
                notice = notice
            )
        }
        startTimer()
    }

    private suspend fun pauseSession() {
        if (!sessionStarted) return
        runCatching { exerciseClient?.pauseExercise() }
        pausedElapsedRealtime = SystemClock.elapsedRealtime()
        ExerciseSessionStore.update { it.copy(phase = ExercisePhase.PAUSED) }
        refreshMetrics()
    }

    private suspend fun resumeSession() {
        if (!sessionStarted) return
        runCatching { exerciseClient?.resumeExercise() }
        if (pausedElapsedRealtime > 0L) {
            totalPausedMillis += SystemClock.elapsedRealtime() - pausedElapsedRealtime
            pausedElapsedRealtime = 0L
        }
        ExerciseSessionStore.update { it.copy(phase = ExercisePhase.ACTIVE) }
        refreshMetrics()
    }

    private suspend fun requestEnd() {
        if (!sessionStarted) {
            stopSelf()
            return
        }
        ending = true
        if (exerciseClient == null) {
            finishSession()
            return
        }
        runCatching { exerciseClient?.endExercise() }
        lifecycleScope.launch {
            delay(3_000L)
            if (sessionStarted) finishSession()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = lifecycleScope.launch {
            while (isActive) {
                refreshMetrics()
                delay(1_000L)
            }
        }
    }

    private fun refreshMetrics() {
        val state = ExerciseSessionStore.state.value
        if (state.phase != ExercisePhase.ACTIVE && state.phase != ExercisePhase.PAUSED) return
        val type = state.type ?: return

        val duration = currentDurationMillis()
        val calories = CalorieCalculator.estimateNetKcal(
            type = type,
            weightKg = weightKg,
            distanceMeters = lastDistanceMeters,
            activeDurationMillis = duration
        )
        val pace = MetricsCalculator.averagePaceSecondsPerKm(
            distanceMeters = lastDistanceMeters,
            activeDurationMillis = duration
        )

        ExerciseSessionStore.update {
            it.copy(
                activeDurationMillis = duration,
                distanceMeters = lastDistanceMeters,
                caloriesKcal = calories,
                paceSecondsPerKm = pace,
                distanceSource = when {
                    lastDistanceMeters == null -> DistanceSource.NONE
                    gpsEnabled -> DistanceSource.GPS
                    else -> DistanceSource.STEPS
                }
            )
        }
    }

    private fun currentDurationMillis(): Long {
        val checkpoint = lastCheckpoint
        if (checkpoint != null) {
            val extra = if (ExerciseSessionStore.state.value.phase == ExercisePhase.ACTIVE) {
                Duration.between(checkpoint.time, Instant.now()).toMillis().coerceAtLeast(0L)
            } else {
                0L
            }
            return checkpoint.activeDuration.toMillis() + extra
        }

        val now = if (ExerciseSessionStore.state.value.phase == ExercisePhase.PAUSED) {
            pausedElapsedRealtime.takeIf { it > 0L } ?: SystemClock.elapsedRealtime()
        } else {
            SystemClock.elapsedRealtime()
        }
        return (now - startElapsedRealtime - totalPausedMillis).coerceAtLeast(0L)
    }

    private fun finishSession() {
        if (!sessionStarted) return
        sessionStarted = false
        ending = false
        timerJob?.cancel()

        val state = ExerciseSessionStore.state.value
        val type = state.type ?: ActivityType.WALKING
        val duration = maxOf(state.activeDurationMillis, currentDurationMillis())
        val distance = lastDistanceMeters
        val calories = CalorieCalculator.estimateNetKcal(
            type = type,
            weightKg = weightKg,
            distanceMeters = distance,
            activeDurationMillis = duration
        )
        val pace = MetricsCalculator.averagePaceSecondsPerKm(distance, duration)
        val endEpochMillis = System.currentTimeMillis()
        val start = startEpochMillis.takeIf { it > 0L } ?: (endEpochMillis - duration)
        val source = when {
            distance == null -> DistanceSource.NONE
            gpsEnabled -> DistanceSource.GPS
            else -> DistanceSource.STEPS
        }

        val activity = ActivityRecord(
            id = UUID.randomUUID().toString(),
            type = type,
            startEpochMillis = start,
            endEpochMillis = endEpochMillis,
            activeDurationMillis = duration,
            distanceMeters = distance,
            caloriesKcal = calories,
            averagePaceSecondsPerKm = pace,
            weightKg = weightKg,
            distanceSource = source,
            synced = false
        )

        ExerciseSessionStore.update {
            it.copy(
                phase = ExercisePhase.FINISHED,
                activeDurationMillis = duration,
                distanceMeters = distance,
                caloriesKcal = calories,
                paceSecondsPerKm = pace,
                summary = activity
            )
        }

        container.persistAndSync(activity)

        runCatching { exerciseClient?.clearUpdateCallbackAsync(updateCallback) }
        exerciseClient = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun fail(message: String) {
        sessionStarted = false
        ending = false
        timerJob?.cancel()
        ExerciseSessionStore.update {
            it.copy(
                phase = ExercisePhase.ERROR,
                errorMessage = message
            )
        }
        runCatching { exerciseClient?.clearUpdateCallbackAsync(updateCallback) }
        exerciseClient = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startForegroundForSession(type: ActivityType, useGps: Boolean) {
        createNotificationChannel()
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                if (type == ActivityType.WALKING) "Caminata en curso" else "Carrera en curso"
            )
            .setContentText("Toca para volver a la actividad")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .build()

        val foregroundType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH or
                if (useGps) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0
        } else {
            0
        }

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            foregroundType
        )
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Actividad en curso",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        timerJob?.cancel()
        runCatching { exerciseClient?.clearUpdateCallbackAsync(updateCallback) }
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.ejemplo.caminarcorrer.wear.action.START"
        const val ACTION_PAUSE = "com.ejemplo.caminarcorrer.wear.action.PAUSE"
        const val ACTION_RESUME = "com.ejemplo.caminarcorrer.wear.action.RESUME"
        const val ACTION_END = "com.ejemplo.caminarcorrer.wear.action.END"
        const val EXTRA_TYPE = "extra_type"
        const val EXTRA_GPS = "extra_gps"

        private const val CHANNEL_ID = "exercise_session"
        private const val NOTIFICATION_ID = 42
    }
}
