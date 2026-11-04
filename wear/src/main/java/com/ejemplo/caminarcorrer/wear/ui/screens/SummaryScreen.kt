package com.ejemplo.caminarcorrer.wear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Text
import com.ejemplo.caminarcorrer.shared.format.Formatters
import com.ejemplo.caminarcorrer.shared.model.ActivityRecord

@Composable
fun SummaryScreen(
    summary: ActivityRecord?,
    pendingCount: Int,
    onDone: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 68.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Resumen",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            if (summary == null) {
                Text(
                    text = "No se pudo generar el resumen.",
                    color = Color(0xFFFFB4AB),
                    fontSize = 11.sp
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = summary.type.displayName,
                        color = Color(0xFFFFB77C),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "·",
                        color = Color(0xFFBFC9C2),
                        fontSize = 9.sp
                    )
                    Text(
                        text = if (pendingCount == 0) {
                            "Sincronizado"
                        } else {
                            "Pendiente ($pendingCount)"
                        },
                        color = if (pendingCount == 0) Color(0xFF8CD5B5) else Color(0xFFFFB77C),
                        fontSize = 9.sp
                    )
                }
                SummaryRow("Duración", Formatters.duration(summary.activeDurationMillis))
                SummaryRow("Distancia", Formatters.distanceKm(summary.distanceMeters))
                SummaryRow("Ritmo", Formatters.pace(summary.averagePaceSecondsPerKm))
                SummaryRow(
                    "Calorías",
                    when {
                        summary.caloriesKcal != null ->
                            Formatters.calories(summary.caloriesKcal)
                        summary.weightKg == null -> "— sin peso"
                        else -> "— sin distancia"
                    }
                )
            }

        }

        Button(
            onClick = onDone,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(44.dp),
            label = { Text("Listo", fontWeight = FontWeight.Bold) }
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFFBFC9C2),
            fontSize = 9.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
