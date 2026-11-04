package com.ejemplo.caminarcorrer.wear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import com.ejemplo.caminarcorrer.shared.format.Formatters
import com.ejemplo.caminarcorrer.wear.exercise.ExerciseMode
import com.ejemplo.caminarcorrer.wear.exercise.ExerciseSessionState

@Composable
fun ActivityScreen(
    state: ExerciseSessionState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onEnd: () -> Unit
) {
    var showEndDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = buildString {
                append(state.type?.displayName ?: "Actividad")
                if (state.mode == ExerciseMode.TIMER_ONLY) append(" · SOLO TIEMPO")
                if (state.isPaused) append(" · PAUSADO")
            },
            color = if (state.isPaused) Color(0xFFFFB77C) else Color(0xFF8CD5B5),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = Formatters.duration(state.activeDurationMillis),
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Metric("Distancia", Formatters.distanceKm(state.distanceMeters))
            Metric("Ritmo", Formatters.pace(state.paceSecondsPerKm))
            Metric("Calorías", Formatters.calories(state.caloriesKcal))
        }

        Text(
            text = when {
                state.mode == ExerciseMode.TIMER_ONLY ->
                    state.notice ?: "Solo tiempo: sin distancia ni calorías"
                state.weightKg == null -> "Sin peso: calorías no estimadas"
                state.distanceMeters == null -> "Sin distancia del reloj"
                state.gpsEnabled -> "GPS habilitado"
                else -> "Distancia por pasos"
            },
            color = Color(0xFFBFC9C2),
            fontSize = 8.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = if (state.isPaused) onResume else onPause,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                label = {
                    Icon(
                        imageVector = if (state.isPaused) {
                            Icons.Rounded.PlayArrow
                        } else {
                            Icons.Rounded.Pause
                        },
                        contentDescription = if (state.isPaused) "Reanudar" else "Pausar",
                        modifier = Modifier.size(22.dp)
                    )
                }
            )
            FilledTonalButton(
                onClick = { showEndDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                label = { Text("Fin") }
            )
        }
    }

    AlertDialog(
        visible = showEndDialog,
        title = { Text("Terminar actividad") },
        text = { Text("¿Quieres guardar y terminar la actividad?") },
        onDismissRequest = { showEndDialog = false },
        confirmButton = {
            Button(
                onClick = {
                    showEndDialog = false
                    onEnd()
                }
            ) {
                Text("Sí")
            }
        },
        dismissButton = {
            FilledTonalButton(onClick = { showEndDialog = false }) {
                Text("Seguir")
            }
        }
    )
}

@Composable
private fun Metric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color(0xFFBFC9C2),
            fontSize = 8.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
