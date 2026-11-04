package com.ejemplo.caminarcorrer.wear.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ejemplo.caminarcorrer.shared.model.ActivityRecord
import com.ejemplo.caminarcorrer.shared.model.ActivityType
import com.ejemplo.caminarcorrer.shared.model.UserProfile
import com.ejemplo.caminarcorrer.wear.WearAppContainer
import com.ejemplo.caminarcorrer.wear.exercise.ExerciseService
import com.ejemplo.caminarcorrer.wear.exercise.ExerciseSessionState
import com.ejemplo.caminarcorrer.wear.exercise.ExerciseSessionStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WearViewModel(private val container: WearAppContainer) : ViewModel() {

    val exerciseState: StateFlow<ExerciseSessionState> = ExerciseSessionStore.state

    val profile: StateFlow<UserProfile?> = container.profileRepository.profile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    val activities: StateFlow<List<ActivityRecord>?> = container.activityRepository.activities.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    val pendingCount: StateFlow<Int> = activities
        .map { list -> list?.count { !it.synced } ?: 0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    fun startExercise(context: Context, type: ActivityType, gpsEnabled: Boolean) {
        val intent = Intent(context, ExerciseService::class.java).apply {
            action = ExerciseService.ACTION_START
            putExtra(ExerciseService.EXTRA_TYPE, type.name)
            putExtra(ExerciseService.EXTRA_GPS, gpsEnabled)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun pause(context: Context) = sendAction(context, ExerciseService.ACTION_PAUSE)

    fun resume(context: Context) = sendAction(context, ExerciseService.ACTION_RESUME)

    fun end(context: Context) = sendAction(context, ExerciseService.ACTION_END)

    fun done() {
        ExerciseSessionStore.reset()
    }

    fun retrySync() {
        viewModelScope.launch {
            container.syncRepository.pushPendingActivities()
        }
    }

    private fun sendAction(context: Context, action: String) {
        context.startService(
            Intent(context, ExerciseService::class.java).apply {
                this.action = action
            }
        )
    }

    companion object {
        fun factory(container: WearAppContainer): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { WearViewModel(container) }
            }
    }
}
