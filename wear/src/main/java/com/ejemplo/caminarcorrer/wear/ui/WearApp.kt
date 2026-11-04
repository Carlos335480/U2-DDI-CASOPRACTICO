package com.ejemplo.caminarcorrer.wear.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ejemplo.caminarcorrer.shared.model.ActivityType
import com.ejemplo.caminarcorrer.wear.WearAppContainer
import com.ejemplo.caminarcorrer.wear.exercise.ExercisePermissions
import com.ejemplo.caminarcorrer.wear.exercise.ExercisePhase
import com.ejemplo.caminarcorrer.wear.ui.screens.ActivityScreen
import com.ejemplo.caminarcorrer.wear.ui.screens.ErrorScreen
import com.ejemplo.caminarcorrer.wear.ui.screens.HomeScreen
import com.ejemplo.caminarcorrer.wear.ui.screens.LoadingScreen
import com.ejemplo.caminarcorrer.wear.ui.screens.PermissionDialog
import com.ejemplo.caminarcorrer.wear.ui.screens.PermissionErrorDialog
import com.ejemplo.caminarcorrer.wear.ui.screens.SummaryScreen
import com.ejemplo.caminarcorrer.wear.ui.theme.CaminarCorrerWearTheme

@Composable
fun WearApp(container: WearAppContainer) {
    CaminarCorrerWearTheme {
        WearAppContent(container)
    }
}

@Composable
private fun WearAppContent(container: WearAppContainer) {
    val context = LocalContext.current
    val viewModel: WearViewModel = viewModel(factory = WearViewModel.factory(container))
    val exerciseState by viewModel.exerciseState.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingCount.collectAsStateWithLifecycle()

    var pendingType by remember { mutableStateOf<ActivityType?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionError by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val activityGranted =
            result[Manifest.permission.ACTIVITY_RECOGNITION] == true ||
                ExercisePermissions.hasActivityRecognition(context)

        if (!activityGranted) {
            permissionError =
                "Sin el permiso de actividad física no se puede registrar la actividad."
            pendingType = null
        } else {
            val gpsEnabled = ExercisePermissions.canUseGps(context)
            pendingType?.let { type ->
                viewModel.startExercise(context, type, gpsEnabled)
            }
            pendingType = null
        }
    }

    fun launchPermissions() {
        val permissions = buildList {
            add(Manifest.permission.ACTIVITY_RECOGNITION)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    when (exerciseState.phase) {
        ExercisePhase.IDLE -> HomeScreen(
            profile = profile,
            pendingCount = pendingCount,
            onStart = { type ->
                pendingType = type
                if (ExercisePermissions.hasActivityRecognition(context)) {
                    val gpsEnabled = ExercisePermissions.canUseGps(context)
                    viewModel.startExercise(context, type, gpsEnabled)
                    pendingType = null
                } else {
                    showPermissionDialog = true
                }
            }
        )

        ExercisePhase.STARTING -> LoadingScreen()

        ExercisePhase.ACTIVE, ExercisePhase.PAUSED -> ActivityScreen(
            state = exerciseState,
            onPause = { viewModel.pause(context) },
            onResume = { viewModel.resume(context) },
            onEnd = { viewModel.end(context) }
        )

        ExercisePhase.FINISHED -> SummaryScreen(
            summary = exerciseState.summary,
            pendingCount = pendingCount,
            onDone = viewModel::done
        )

        ExercisePhase.ERROR -> ErrorScreen(
            message = exerciseState.errorMessage ?: "Error desconocido.",
            onBack = viewModel::done
        )
    }

    if (showPermissionDialog) {
        PermissionDialog(
            onConfirm = {
                showPermissionDialog = false
                launchPermissions()
            },
            onDismiss = {
                showPermissionDialog = false
                pendingType = null
            }
        )
    }

    permissionError?.let { message ->
        PermissionErrorDialog(
            message = message,
            onDismiss = { permissionError = null }
        )
    }
}
