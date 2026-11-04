package com.ejemplo.caminarcorrer.wear.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Text

@Composable
fun PermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        visible = true,
        title = { Text("Permisos") },
        text = {
            Text(
                text = "Permiso de actividad física para medir distancia y calorías. " +
                    "La ubicación es opcional: sin ella se usa el podómetro. " +
                    "También pediremos notificaciones para la actividad en curso.",
                fontSize = 11.sp
            )
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Permitir", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            FilledTonalButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun PermissionErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        visible = true,
        title = { Text("Permiso necesario") },
        text = { Text(text = message, fontSize = 11.sp) },
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Entendido")
            }
        }
    )
}
