package com.ejemplo.caminarcorrer.wear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import com.ejemplo.caminarcorrer.shared.format.Formatters
import com.ejemplo.caminarcorrer.shared.model.ActivityType
import com.ejemplo.caminarcorrer.shared.model.UserProfile

@Composable
fun HomeScreen(
    profile: UserProfile?,
    pendingCount: Int,
    onStart: (ActivityType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Caminar",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "y Correr",
                color = Color(0xFFBFC9C2),
                fontSize = 12.sp
            )
        }
        Text(
            text = if (profile != null) {
                "Peso: ${Formatters.weight(profile.weightKg)}"
            } else {
                "Configura tu peso en el teléfono"
            },
            color = if (profile != null) Color(0xFFBFC9C2) else Color(0xFFFFB77C),
            fontSize = 10.sp
        )
        Text(
            text = if (pendingCount == 0) {
                "Todo sincronizado"
            } else {
                "Pendientes: $pendingCount"
            },
            color = if (pendingCount == 0) Color(0xFF8CD5B5) else Color(0xFFFFB77C),
            fontSize = 10.sp
        )

        Button(
            onClick = { onStart(ActivityType.WALKING) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.DirectionsWalk,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Caminar", fontWeight = FontWeight.Bold)
                }
            }
        )

        FilledTonalButton(
            onClick = { onStart(ActivityType.RUNNING) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.DirectionsRun,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Correr", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
