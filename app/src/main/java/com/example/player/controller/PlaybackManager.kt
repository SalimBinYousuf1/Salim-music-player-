package com.example.player.controller

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.domain.model.*
import com.example.player.audio.NativeAudioEffects
import com.example.player.audio.NativeAudioVisualizer
import com.example.player.queue.QueueManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@OptIn(UnstableApi::class)
class PlaybackManager(
    private val context: Context,
    val queueManager: QueueManager = QueueManager(),
    private val onTrackCompleted: (Song) -> Unit = {},
    private val onPlaybackPositionSaved: (songId: Long, position: Long) -> Unit = { _, _ -> }
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    val audioEffects = NativeAudioEffects()
    val visualizer = NativeAudioVisualizer()

    val abLooper = AbLooper { targetMs ->
        seekTo(targetMs)
    }

    val sleepTimer = SleepTimerManager { fadeOut ->
        if (fadeOut) {
            scope.launch {
                val origVol = player.volume
                for (i in 10 downTo 1) {
                    player.volume = origVol * (i / 10f)
                    delay(300)
                }
                pause()
                player.volume = origVol
            }
        } else {
            pause()
        }
    }

    val player: ExoPlayer by lazy {
        ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build().apply {
                addListener(playerListener)
            }
    }

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlaybackState()
            if (isPlaying) {
                startProgressLoop()
            } else {
                stopProgressLoop()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    _uiState.value = _uiState.value.copy(isBuffering = true)
                }
                Player.STATE_READY -> {
                    _uiState.value = _uiState.value.copy(
                        isBuffering = false,
                        duration = player.duration.coerceAtLeast(0L)
                    )
                    initAudioEffectsIfReady()
                }
                Player.STATE_ENDED -> {
                    queueManager.currentSong?.let { onTrackCompleted(it) }
                    playNext()
                }
                Player.STATE_IDLE -> {
                    _uiState.value = _uiState.value.copy(isBuffering = false)
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            error.printStackTrace()
            _uiState.value = _uiState.value.copy(isBuffering = false, isPlaying = false)
        }
    }

    private fun initAudioEffectsIfReady() {
        val audioSessionId = player.audioSessionId
        if (audioSessionId != C.AUDIO_SESSION_ID_UNSET && audioSessionId != 0) {
            audioEffects.init(audioSessionId, _uiState.value.audioEffects)
            visualizer.start(audioSessionId)
        }
    }

    fun playQueue(songs: List<Song>, startIndex: Int = 0, initialPositionMs: Long = 0L) {
        if (songs.isEmpty()) return
        queueManager.setQueue(songs, startIndex)
        val targetSong = queueManager.currentSong ?: return
        loadAndPlay(targetSong, initialPositionMs)
    }

    fun playSong(song: Song, initialPositionMs: Long = 0L) {
        val currentQueue = queueManager.queue.value
        val existingIndex = currentQueue.indexOfFirst { it.id == song.id }
        if (existingIndex != -1) {
            queueManager.setCurrentIndex(existingIndex)
            loadAndPlay(song, initialPositionMs)
        } else {
            playQueue(listOf(song), 0, initialPositionMs)
        }
    }

    private fun loadAndPlay(song: Song, startPositionMs: Long = 0L) {
        try {
            val metadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setAlbumTitle(song.album)
                .setArtworkUri(song.artworkUri?.let { Uri.parse(it) })
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(song.fileUri)
                .setMediaId(song.id.toString())
                .setMediaMetadata(metadata)
                .build()

            player.setMediaItem(mediaItem)
            if (startPositionMs > 0) {
                player.seekTo(startPositionMs)
            }
            player.prepare()
            player.play()

            _uiState.value = _uiState.value.copy(
                currentSong = song,
                queue = queueManager.queue.value,
                queueIndex = queueManager.currentIndex.value,
                currentPosition = startPositionMs,
                duration = if (song.duration > 0) song.duration else 0L
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_IDLE && queueManager.currentSong != null) {
                loadAndPlay(queueManager.currentSong!!)
            } else {
                player.play()
            }
        }
        updatePlaybackState()
    }

    fun play() {
        if (!player.isPlaying) {
            player.play()
            updatePlaybackState()
        }
    }

    fun pause() {
        if (player.isPlaying) {
            player.pause()
            updatePlaybackState()
        }
    }

    fun playNext() {
        val nextSong = queueManager.moveToNext()
        if (nextSong != null) {
            loadAndPlay(nextSong)
        } else {
            player.stop()
            _uiState.value = _uiState.value.copy(isPlaying = false, currentPosition = 0L)
        }
    }

    fun playPrevious() {
        if (player.currentPosition > 3000) {
            // Seek to start of current song if passed 3s
            seekTo(0)
            return
        }
        val prevSong = queueManager.moveToPrevious()
        if (prevSong != null) {
            loadAndPlay(prevSong)
        }
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, player.duration.coerceAtLeast(0L))
        player.seekTo(clamped)
        _uiState.value = _uiState.value.copy(currentPosition = clamped)
    }

    fun seekForward10s() {
        seekTo(player.currentPosition + 10000L)
    }

    fun seekBackward10s() {
        seekTo(player.currentPosition - 10000L)
    }

    fun setPlaybackSpeed(speed: Float) {
        player.playbackParameters = PlaybackParameters(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun setShuffle(enabled: Boolean) {
        queueManager.setShuffle(enabled)
        _uiState.value = _uiState.value.copy(isShuffleOn = enabled)
    }

    fun toggleRepeatMode() {
        val nextMode = (_uiState.value.repeatMode + 1) % 3
        queueManager.setRepeatMode(nextMode)
        _uiState.value = _uiState.value.copy(repeatMode = nextMode)
    }

    fun setStereoBalance(balance: Float) {
        val clamped = balance.coerceIn(-1f, 1f)
        _uiState.value = _uiState.value.copy(
            audioEffects = _uiState.value.audioEffects.copy(stereoBalance = clamped)
        )
        // Adjust left/right volume balance on ExoPlayer
        val left = if (clamped > 0) 1f - clamped else 1f
        val right = if (clamped < 0) 1f + clamped else 1f
        // Combined average multiplier
        player.volume = ((left + right) / 2f).coerceIn(0f, 1f)
    }

    fun setMonoAudio(mono: Boolean) {
        _uiState.value = _uiState.value.copy(
            audioEffects = _uiState.value.audioEffects.copy(monoAudio = mono)
        )
    }

    fun updateAudioEffects(newEffects: AudioEffectSettings) {
        _uiState.value = _uiState.value.copy(audioEffects = newEffects)
        audioEffects.setEqualizerEnabled(newEffects.equalizerEnabled)
        audioEffects.applyBandLevels(newEffects.bandLevels)
        audioEffects.setBassBoost(newEffects.bassBoostEnabled, newEffects.bassBoostStrength)
        audioEffects.setVirtualizer(newEffects.virtualizerEnabled, newEffects.virtualizerStrength)
        audioEffects.setLoudnessGain(newEffects.loudnessEnhancerEnabled, newEffects.loudnessGain)
    }

    private fun startProgressLoop() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val pos = player.currentPosition
                val dur = player.duration.coerceAtLeast(0L)
                val buf = player.bufferedPosition

                _uiState.value = _uiState.value.copy(
                    currentPosition = pos,
                    duration = dur,
                    bufferedPosition = buf,
                    isPlaying = player.isPlaying
                )

                // Check A-B loop
                abLooper.checkLoop(pos)

                // Drive simulated fallback visualizer if native audio visualizer unavailable
                visualizer.tickSimulatedFallback(player.isPlaying)

                // Periodically notify position for persistence
                queueManager.currentSong?.let { song ->
                    onPlaybackPositionSaved(song.id, pos)
                }

                delay(200)
            }
        }
    }

    private fun stopProgressLoop() {
        progressJob?.cancel()
        progressJob = null
        updatePlaybackState()
    }

    private fun updatePlaybackState() {
        _uiState.value = _uiState.value.copy(
            isPlaying = player.isPlaying,
            currentPosition = player.currentPosition,
            duration = player.duration.coerceAtLeast(0L),
            queue = queueManager.queue.value,
            queueIndex = queueManager.currentIndex.value
        )
    }

    fun release() {
        progressJob?.cancel()
        audioEffects.release()
        visualizer.stop()
        player.removeListener(playerListener)
        player.release()
    }
}
