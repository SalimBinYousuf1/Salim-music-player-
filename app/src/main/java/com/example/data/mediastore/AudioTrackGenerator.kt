package com.example.data.mediastore

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

object AudioTrackGenerator {

    /**
     * Generates a real native playable PCM 16-bit 44.1kHz WAV file on device storage.
     * Generates a warm musical chord/arpeggio practice track with rhythm.
     */
    suspend fun generatePracticeTrack(
        context: Context,
        title: String,
        durationSeconds: Int = 15,
        baseFrequency: Double = 261.63 // C4
    ): File = withContext(Dispatchers.IO) {
        val audioDir = File(context.filesDir, "audio_tracks").apply { mkdirs() }
        val cleanFileName = title.lowercase().replace(Regex("[^a-z0-9_]"), "_") + ".wav"
        val outputFile = File(audioDir, cleanFileName)

        val sampleRate = 44100
        val numChannels = 2 // Stereo
        val bitsPerSample = 16
        val totalSamples = durationSeconds * sampleRate
        val byteRate = sampleRate * numChannels * (bitsPerSample / 8)
        val blockAlign = numChannels * (bitsPerSample / 8)
        val dataSize = totalSamples * blockAlign

        FileOutputStream(outputFile).use { fos ->
            // Write standard 44-byte WAV header
            val header = ByteBuffer.allocate(44).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                // "RIFF" chunk
                put('R'.code.toByte())
                put('I'.code.toByte())
                put('F'.code.toByte())
                put('F'.code.toByte())
                putInt(36 + dataSize) // ChunkSize
                // "WAVE" format
                put('W'.code.toByte())
                put('A'.code.toByte())
                put('V'.code.toByte())
                put('E'.code.toByte())
                // "fmt " subchunk
                put('f'.code.toByte())
                put('m'.code.toByte())
                put('t'.code.toByte())
                put(' '.code.toByte())
                putInt(16) // Subchunk1Size for PCM
                putShort(1.toShort()) // AudioFormat 1 = PCM
                putShort(numChannels.toShort())
                putInt(sampleRate)
                putInt(byteRate)
                putShort(blockAlign.toShort())
                putShort(bitsPerSample.toShort())
                // "data" subchunk
                put('d'.code.toByte())
                put('a'.code.toByte())
                put('t'.code.toByte())
                put('a'.code.toByte())
                putInt(dataSize)
            }
            fos.write(header.array())

            // Musical chord notes (Major triad: 1, 5/4, 3/2, 2)
            val chordRatios = listOf(1.0, 1.25, 1.5, 2.0)
            val buffer = ByteArray(4096)
            var bufferPos = 0

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / sampleRate
                // Chord progression cycle every 2 seconds
                val chordIndex = ((t / 1.5).toInt()) % 4
                val rootMultiplier = when (chordIndex) {
                    0 -> 1.0       // Root
                    1 -> 1.3348    // Fourth
                    2 -> 1.4983    // Fifth
                    else -> 1.1892 // Minor third
                }
                val freq = baseFrequency * rootMultiplier

                // Tone generation with harmonic richness
                var sample = 0.0
                for ((idx, ratio) in chordRatios.withIndex()) {
                    val noteFreq = freq * ratio
                    val harmonic = sin(2.0 * PI * noteFreq * t) * (0.35 / (idx + 1))
                    sample += harmonic
                }

                // Envelope attack/decay for rhythmic pulse every beat (0.5s)
                val beatPhase = (t % 0.5) / 0.5
                val envelope = (1.0 - beatPhase) * 0.8 + 0.2
                sample *= envelope

                // Subtle stereo panning movement
                val pan = sin(2.0 * PI * 0.2 * t) // -1 to 1
                val leftGain = 0.5 * (1.0 - pan)
                val rightGain = 0.5 * (1.0 + pan)

                val leftSampleShort = (sample * leftGain * 24000).toInt().coerceIn(-32768, 32767).toShort()
                val rightSampleShort = (sample * rightGain * 24000).toInt().coerceIn(-32768, 32767).toShort()

                // Left channel (little-endian)
                buffer[bufferPos++] = (leftSampleShort.toInt() and 0xFF).toByte()
                buffer[bufferPos++] = ((leftSampleShort.toInt() shr 8) and 0xFF).toByte()
                // Right channel (little-endian)
                buffer[bufferPos++] = (rightSampleShort.toInt() and 0xFF).toByte()
                buffer[bufferPos++] = ((rightSampleShort.toInt() shr 8) and 0xFF).toByte()

                if (bufferPos >= buffer.size) {
                    fos.write(buffer, 0, bufferPos)
                    bufferPos = 0
                }
            }

            if (bufferPos > 0) {
                fos.write(buffer, 0, bufferPos)
            }
        }

        outputFile
    }
}
