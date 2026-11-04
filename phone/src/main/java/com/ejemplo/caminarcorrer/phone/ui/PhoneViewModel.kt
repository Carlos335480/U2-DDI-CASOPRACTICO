package com.ejemplo.caminarcorrer.phone.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ejemplo.caminarcorrer.phone.PhoneAppContainer
import com.ejemplo.caminarcorrer.shared.model.ActivityRecord
import com.ejemplo.caminarcorrer.shared.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PhoneUiState(
    val activities: List<ActivityRecord>? = null,
    val profile: UserProfile? = null,
    val syncMessage: String? = null
)

class PhoneViewModel(private val container: PhoneAppContainer) : ViewModel() {

    private val syncMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PhoneUiState> = combine(
        container.activityRepository.activities,
        container.profileRepository.profile,
        syncMessage
    ) { activities, profile, message ->
        PhoneUiState(
            activities = activities,
            profile = profile,
            syncMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PhoneUiState()
    )

    val dataLayerAvailable: Boolean
        get() = container.syncRepository.isAvailable

    fun saveWeight(input: String): String? {
        val normalized = input.trim().replace(',', '.')
        if (normalized.isEmpty()) return "Ingresa tu peso."
        val weight = normalized.toDoubleOrNull() ?: return "Usa un número válido."
        if (weight !in 20.0..300.0) return "El peso debe estar entre 20 y 300 kg."

        viewModelScope.launch {
            val profile = UserProfile(
                weightKg = weight,
                updatedAtEpochMillis = System.currentTimeMillis()
            )
            container.profileRepository.save(profile)
            val sent = container.syncRepository.pushProfile()
            syncMessage.value = when {
                sent -> "Perfil guardado. Se sincronizará con el reloj."
                !container.syncRepository.isAvailable ->
                    "Perfil guardado. Google Play services no está disponible; no se puede sincronizar."
                else -> "Perfil guardado. Se reintentará la sincronización."
            }
        }
        return null
    }

    fun requestSync() {
        if (!container.syncRepository.isAvailable) {
            syncMessage.value = "Google Play services no está disponible."
            return
        }
        viewModelScope.launch {
            val requested = container.syncRepository.requestSync()
            syncMessage.value = if (requested) {
                "Sincronización solicitada al reloj."
            } else {
                "No hay un reloj conectado."
            }
        }
    }

    fun clearMessage() {
        syncMessage.value = null
    }

    companion object {
        fun factory(container: PhoneAppContainer): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { PhoneViewModel(container) }
            }
    }
}
