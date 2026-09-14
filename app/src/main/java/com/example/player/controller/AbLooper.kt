package com.example.player.controller

import com.example.domain.model.AbLoopState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AbLooper(
    private val seekTo: (Long) -> Unit
) {
    private val _state = MutableStateFlow(AbLoopState())
    val state: StateFlow<AbLoopState> = _state.asStateFlow()

    fun setPointA(positionMs: Long) {
        val current = _state.value
        val validB = if (current.pointB != null && current.pointB > positionMs) current.pointB else null
        _state.value = current.copy(pointA = positionMs, pointB = validB, isEnabled = validB != null)
    }

    fun setPointB(positionMs: Long) {
        val current = _state.value
        val a = current.pointA ?: 0L
        if (positionMs > a) {
            _state.value = current.copy(pointA = a, pointB = positionMs, isEnabled = true)
        }
    }

    fun toggleLoop() {
        val current = _state.value
        if (current.pointA != null && current.pointB != null) {
            _state.value = current.copy(isEnabled = !current.isEnabled)
        }
    }

    fun clearLoop() {
        _state.value = AbLoopState()
    }

    fun checkLoop(currentPositionMs: Long) {
        val loop = _state.value
        if (loop.isEnabled && loop.pointA != null && loop.pointB != null) {
            if (currentPositionMs >= loop.pointB) {
                seekTo(loop.pointA)
            }
        }
    }
}
