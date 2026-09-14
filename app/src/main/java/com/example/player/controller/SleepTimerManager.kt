package com.example.player.controller

import com.example.domain.model.SleepTimerState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SleepTimerManager(
    private val onTimerExpired: (fadeOut: Boolean) -> Unit
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null

    private val _state = MutableStateFlow(SleepTimerState())
    val state: StateFlow<SleepTimerState> = _state.asStateFlow()

    fun startTimer(minutes: Int, fadeOut: Boolean = true) {
        cancelTimer()
        val totalSec = minutes * 60L
        _state.value = SleepTimerState(
            isActive = true,
            totalSeconds = totalSec,
            remainingSeconds = totalSec,
            fadeOutBeforePause = fadeOut
        )

        timerJob = scope.launch {
            var remaining = totalSec
            while (remaining > 0) {
                delay(1000)
                remaining--
                _state.value = _state.value.copy(remainingSeconds = remaining)
            }
            _state.value = SleepTimerState()
            onTimerExpired(fadeOut)
        }
    }

    fun extendTimer(additionalMinutes: Int = 5) {
        val current = _state.value
        if (current.isActive) {
            val addSec = additionalMinutes * 60L
            val newTotal = current.totalSeconds + addSec
            val newRemaining = current.remainingSeconds + addSec
            _state.value = current.copy(totalSeconds = newTotal, remainingSeconds = newRemaining)
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _state.value = SleepTimerState()
    }
}
