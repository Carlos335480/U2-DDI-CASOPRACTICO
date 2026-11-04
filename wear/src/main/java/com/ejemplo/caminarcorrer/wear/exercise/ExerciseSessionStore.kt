package com.ejemplo.caminarcorrer.wear.exercise

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object ExerciseSessionStore {

    private val _state = MutableStateFlow(ExerciseSessionState())
    val state: StateFlow<ExerciseSessionState> = _state.asStateFlow()

    fun update(transform: (ExerciseSessionState) -> ExerciseSessionState) {
        _state.update(transform)
    }

    fun set(value: ExerciseSessionState) {
        _state.value = value
    }

    fun reset() {
        _state.value = ExerciseSessionState()
    }
}
