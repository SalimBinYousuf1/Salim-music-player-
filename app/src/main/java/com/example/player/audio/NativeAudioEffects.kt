package com.example.player.audio

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.example.domain.model.AudioEffectSettings
import com.example.domain.model.EqualizerPreset

class NativeAudioEffects {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var audioSessionId: Int = 0

    var isSupported: Boolean = true
        private set

    fun init(audioSessionId: Int, initialSettings: AudioEffectSettings) {
        if (audioSessionId == 0) return
        this.audioSessionId = audioSessionId
        release()

        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = initialSettings.equalizerEnabled
                applyBandLevels(initialSettings.bandLevels)
            }
        } catch (e: Exception) {
            Log.w("NativeAudioEffects", "Equalizer init failed: ${e.message}")
            isSupported = false
        }

        try {
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = initialSettings.bassBoostEnabled
                setStrength(initialSettings.bassBoostStrength.toShort())
            }
        } catch (e: Exception) {
            Log.w("NativeAudioEffects", "BassBoost init failed: ${e.message}")
        }

        try {
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = initialSettings.virtualizerEnabled
                setStrength(initialSettings.virtualizerStrength.toShort())
            }
        } catch (e: Exception) {
            Log.w("NativeAudioEffects", "Virtualizer init failed: ${e.message}")
        }

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                    enabled = initialSettings.loudnessEnhancerEnabled
                    setTargetGain(initialSettings.loudnessGain)
                }
            }
        } catch (e: Exception) {
            Log.w("NativeAudioEffects", "LoudnessEnhancer init failed: ${e.message}")
        }
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBandLevel(bandIndex: Int, levelMillibels: Int) {
        try {
            val eq = equalizer ?: return
            val numBands = eq.numberOfBands.toInt()
            if (bandIndex in 0 until numBands) {
                val range = eq.bandLevelRange // [min, max]
                val clamped = levelMillibels.coerceIn(range[0].toInt(), range[1].toInt()).toShort()
                eq.setBandLevel(bandIndex.toShort(), clamped)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun applyBandLevels(levels: List<Int>) {
        try {
            val eq = equalizer ?: return
            val numBands = eq.numberOfBands.toInt()
            for (i in 0 until minOf(levels.size, numBands)) {
                setBandLevel(i, levels[i])
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun applyPreset(preset: EqualizerPreset) {
        val levels = listOf(preset.band60Hz, preset.band250Hz, preset.band1kHz, preset.band4kHz, preset.band12kHz)
        applyBandLevels(levels)
        setBassBoost(preset.bassBoost > 0, preset.bassBoost)
        setVirtualizer(preset.virtualizer > 0, preset.virtualizer)
        setLoudnessGain(preset.loudnessGain > 0, preset.loudnessGain)
    }

    fun setBassBoost(enabled: Boolean, strength: Int) {
        try {
            bassBoost?.apply {
                this.enabled = enabled
                setStrength(strength.coerceIn(0, 1000).toShort())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setVirtualizer(enabled: Boolean, strength: Int) {
        try {
            virtualizer?.apply {
                this.enabled = enabled
                setStrength(strength.coerceIn(0, 1000).toShort())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setLoudnessGain(enabled: Boolean, gainMillibels: Int) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                loudnessEnhancer?.apply {
                    this.enabled = enabled
                    setTargetGain(gainMillibels)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        try { equalizer?.release() } catch (_: Exception) {}
        try { bassBoost?.release() } catch (_: Exception) {}
        try { virtualizer?.release() } catch (_: Exception) {}
        try { loudnessEnhancer?.release() } catch (_: Exception) {}
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
    }
}
