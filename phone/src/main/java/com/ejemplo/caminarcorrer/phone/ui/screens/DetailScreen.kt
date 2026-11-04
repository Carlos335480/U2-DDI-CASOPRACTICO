package com.ejemplo.caminarcorrer.phone.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ejemplo.caminarcorrer.shared.format.Formatters
import com.ejemplo.caminarcorrer.shared.model.ActivityRecord
import com.ejemplo.caminarcorrer.shared.model.ActivityType
import com.ejemplo.caminarcorrer.shared.model.DistanceSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    activitiesLoaded: Boolean,
    activity: ActivityRecord?,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            !activitiesLoaded -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            activity == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se encontró la actividad.")
                }
            }

            else -> {
                DetailContent(
                    activity = activity,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailContent(
    activity: ActivityRecord,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (activity.type == ActivityType.WALKING) {
                        Icons.Rounded.DirectionsWalk
                    } else {
                        Icons.Rounded.DirectionsRun
                    },
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.size(16.dp))
                Column {
                    Text(
                        text = activity.type.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = Formatters.date(activity.startEpochMillis),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                label = "Duración",
                value = Formatters.duration(activity.activeDurationMillis),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Distancia",
                value = Formatters.distanceKm(activity.distanceMeters),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                label = "Ritmo promedio",
                value = Formatters.pace(activity.averagePaceSecondsPerKm),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Calorías",
                value = Formatters.calories(activity.caloriesKcal),
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.size(12.dp))
                Text(
                    text = "Actividad recibida en el teléfono.",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        InfoCard(
            title = "Origen de la distancia",
            text = when (activity.distanceSource) {
                DistanceSource.GPS ->
                    "Health Services reportó la distancia con GPS o sensores del reloj."
                DistanceSource.STEPS ->
                    "La distancia es una estimación por pasos. Puede variar según el reloj y la zancada."
                DistanceSource.NONE ->
                    "El reloj no reportó distancia para esta actividad. No se inventó un valor."
            }
        )

        InfoCard(
            title = "Sobre las calorías",
            text = if (activity.caloriesKcal != null) {
                "Estimación neta basada en el peso (${Formatters.weight(activity.weightKg)}), " +
                    "el tipo de actividad y la velocidad promedio. No es una medición médica."
            } else {
                "No se muestran calorías porque faltó el peso del perfil o la distancia. " +
                    "Configura tu peso en el teléfono para futuras actividades."
            }
        )

        InfoCard(
            title = "Privacidad",
            text = "El perfil y el historial se guardan localmente en el teléfono y en el reloj. " +
                "No hay cuentas, anuncios ni analítica. La sincronización usa Wear OS Data Layer.",
            icon = Icons.Rounded.Info
        )
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Rounded.Info
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
