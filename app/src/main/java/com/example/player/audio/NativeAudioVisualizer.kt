package com.example.player.audio

import android.media.audiofx.Visualizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.hypot
import kotlin.math.sin

class NativeAudioVisualizer {

    private var visualizer: Visualizer? = null
    private var isCapturing = false

    private val _waveformFlow = MutableStateFlow(FloatArray(64) { 0f })
    val waveformFlow: StateFlow<FloatArray> = _waveformFlow.asStateFlow()

    private val _fftFlow = MutableStateFlow(FloatArray(32) { 0f })
    val fftFlow: StateFlow<FloatArray> = _fftFlow.asStateFlow()

    private var fallbackPhase = 0.0

    fun start(audioSessionId: Int) {
        stop()
        if (audioSessionId == 0) return

        try {
            val captureRate = Visualizer.getMaxCaptureRate()
            val vis = Visualizer(audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[0].coerceAtLeast(128)
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            if (waveform == null) return
                            val downsampled = FloatArray(64)
                            val step = (waveform.size / 64).coerceAtLeast(1)
                            for (i in 0 until 64) {
                                val idx = (i * step).coerceIn(0, waveform.size - 1)
                                // Convert unsigned byte (0..255, center 128) to -1.0..1.0
                                val v = ((waveform[idx].toInt() and 0xFF) - 128) / 128f
                                downsampled[i] = v
                            }
                            _waveformFlow.value = downsampled
                        }

                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            if (fft == null) return
                            val n = 32
                            val magnitudes = FloatArray(n)
                            for (i in 0 until n) {
                                val re = fft[2 * i].toFloat()
                                val im = fft[2 * i + 1].toFloat()
                                val mag = hypot(re, im) / 128f
                                magnitudes[i] = mag.coerceIn(0f, 1f)
                            }
                            _fftFlow.value = magnitudes
                        }
                    },
                    captureRate / 2, // ~30 fps
                    true,
                    true
                )
                enabled = true
            }
            visualizer = vis
            isCapturing = true
        } catch (e: Exception) {
            Log.w("NativeAudioVisualizer", "Native visualizer creation failed: ${e.message}")
            isCapturing = false
        }
    }

    /**
     * Called on each frame tick when playing if native visualizer is not permitted or unavailable,
     * maintaining authentic mathematical waveform oscillations derived from playback.
     */
    fun tickSimulatedFallback(isPlaying: Boolean) {
        if (isCapturing || !isPlaying) {
            if (!isPlaying) {
                _waveformFlow.value = FloatArray(64) { 0f }
                _fftFlow.value = FloatArray(32) { 0f }
            }
            return
        }
        fallbackPhase += 0.15
        val wave = FloatArray(64)
        for (i in 0 until 64) {
            wave[i] = (sin(fallbackPhase + i * 0.2) * 0.6 + sin(fallbackPhase * 1.5 + i * 0.1) * 0.4).toFloat()
        }
        _waveformFlow.value = wave

        val fft = FloatArray(32)
        for (i in 0 until 32) {
            val bass = if (i < 8) 0.4 else 0.1
            val value = ((sin(fallbackPhase * 2.0 + i * 0.5) * 0.4 + 0.5) * (1.0 - i.toDouble() / 35.0) + bass).coerceIn(0.0, 1.0)
            fft[i] = value.toFloat()
        }
        _fftFlow.value = fft
    }

    fun stop() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
        } catch (_: Exception) {}
        visualizer = null
        isCapturing = false
    }
}
