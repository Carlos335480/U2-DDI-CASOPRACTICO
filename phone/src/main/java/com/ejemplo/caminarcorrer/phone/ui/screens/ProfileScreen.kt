package com.ejemplo.caminarcorrer.phone.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ejemplo.caminarcorrer.shared.format.Formatters
import com.ejemplo.caminarcorrer.shared.logic.CalorieCalculator
import com.ejemplo.caminarcorrer.shared.model.UserProfile
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profile: UserProfile?,
    message: String?,
    onBack: () -> Unit,
    onSave: (String) -> String?,
    onClearMessage: () -> Unit
) {
    var weightInput by remember(profile) {
        mutableStateOf(
            profile?.weightKg?.let { String.format(Locale.US, "%.1f", it) } ?: ""
        )
    }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.size(16.dp))
                    Column {
                        Text(
                            text = "Peso corporal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (profile != null) {
                                "Actual: ${Formatters.weight(profile.weightKg)}"
                            } else {
                                "Sin configurar"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = "El peso se usa únicamente para estimar las calorías de tus actividades. " +
                    "Se guarda en este teléfono y se envía al reloj mediante Wear OS.",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = weightInput,
                onValueChange = {
                    weightInput = it
                    error = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Peso") },
                supportingText = {
                    Text(
                        text = error ?: "Ingresa tu peso en kilogramos (20 a 300 kg)."
                    )
                },
                isError = error != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Button(
                onClick = {
                    error = onSave(weightInput)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar peso")
            }

            message?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        TextButton(onClick = onClearMessage) {
                            Text("Cerrar")
                        }
                    }
                }
            }

            InfoCard(
                title = "Cómo se calculan las calorías",
                text = CalorieCalculator.FORMULA_DESCRIPTION
            )

            InfoCard(
                title = "Limitaciones",
                text = "Es una estimación poblacional. No considera edad, sexo, composición corporal, " +
                    "terreno, temperatura, frecuencia cardiaca ni eficiencia individual. " +
                    "No es una medición médica."
            )

            InfoCard(
                title = "Privacidad",
                text = "No hay cuentas, anuncios ni analítica. El peso y el historial se guardan " +
                    "localmente. La sincronización usa Wear OS Data Layer y puede apoyarse en " +
                    "los servidores de Google cuando no hay Bluetooth."
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    text: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = Icons.Rounded.Info,
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
