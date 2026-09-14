package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.backup.BackupRestoreManager
import com.example.domain.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SalimApplication
    private val repository = app.repository
    private val preferences = app.preferencesManager
    val playbackManager = app.playbackManager
    val backupManager = app.backupRestoreManager

    // Preferences Flows
    val appTheme: StateFlow<SalimTheme> = preferences.appTheme
        .stateIn(viewModelScope, SharingStarted.Eagerly, SalimTheme.DARK_SLATE)
    val themeMode: StateFlow<ThemeMode> = preferences.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)
    val dynamicColors: StateFlow<Boolean> = preferences.dynamicColors
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val highContrast: StateFlow<Boolean> = preferences.highContrast
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val compactMode: StateFlow<Boolean> = preferences.compactMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val reducedMotion: StateFlow<Boolean> = preferences.reducedMotion
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val sortOption: StateFlow<SortOption> = preferences.sortOption
        .stateIn(viewModelScope, SharingStarted.Eagerly, SortOption.TITLE_AZ)
    val gaplessPlayback: StateFlow<Boolean> = preferences.gaplessPlayback
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val autoScrollLyrics: StateFlow<Boolean> = preferences.autoScrollLyrics
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val keepScreenAwake: StateFlow<Boolean> = preferences.keepScreenAwake
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val lyricsTextSize: StateFlow<Float> = preferences.lyricsTextSize
        .stateIn(viewModelScope, SharingStarted.Eagerly, 18f)

    // Player State
    val playerUiState: StateFlow<PlayerUiState> = playbackManager.uiState
    val waveformFlow: StateFlow<FloatArray> = playbackManager.visualizer.waveformFlow
    val fftFlow: StateFlow<FloatArray> = playbackManager.visualizer.fftFlow

    // Library Data
    val rawSongs: StateFlow<List<Song>> = repository.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val selectedGenreFilter = MutableStateFlow<String?>(null)

    val sortedAndFilteredSongs: StateFlow<List<Song>> = combine(
        rawSongs,
        sortOption,
        selectedGenreFilter
    ) { songs, sort, genre ->
        val filtered = if (genre != null && genre != "All") {
            songs.filter { it.genre.equals(genre, ignoreCase = true) }
        } else {
            songs
        }
        applySort(filtered, sort)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favorites: StateFlow<List<Song>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentlyPlayed: StateFlow<List<Song>> = repository.getRecentlyPlayed()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val mostPlayed: StateFlow<List<Song>> = repository.getMostPlayed()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val playlists: StateFlow<List<Playlist>> = repository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val genres: StateFlow<List<String>> = repository.getDistinctGenres()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val albums: StateFlow<List<String>> = repository.getDistinctAlbums()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val artists: StateFlow<List<String>> = repository.getDistinctArtists()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val presets: StateFlow<List<EqualizerPreset>> = repository.getAllPresets()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Search
    val searchQuery = MutableStateFlow("")
    val searchResults: StateFlow<List<Song>> = searchQuery
        .debounce(250)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList()) else repository.searchSongs(query)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Batch Selection
    val selectedSongIds = MutableStateFlow<Set<Long>>(emptySet())
    val isBatchMode = selectedSongIds.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // Scanning State
    val isScanning = MutableStateFlow(false)
    val scanProgress = MutableStateFlow(0f)

    init {
        // Restore last playback position if available
        viewModelScope.launch {
            val lastId = preferences.lastSongId.first()
            val lastPos = preferences.lastPosition.first()
            if (lastId > 0) {
                val song = repository.getSongByIdDirect(lastId)
                if (song != null) {
                    playbackManager.playSong(song, lastPos)
                    playbackManager.pause()
                }
            }
        }
    }

    private fun applySort(list: List<Song>, sort: SortOption): List<Song> {
        return when (sort) {
            SortOption.TITLE_AZ -> list.sortedBy { it.title.lowercase() }
            SortOption.TITLE_ZA -> list.sortedByDescending { it.title.lowercase() }
            SortOption.RECENTLY_ADDED -> list.sortedByDescending { it.dateAdded }
            SortOption.RECENTLY_PLAYED -> list.sortedByDescending { it.lastPlayedTimestamp }
            SortOption.MOST_PLAYED -> list.sortedByDescending { it.playCount }
            SortOption.LEAST_PLAYED -> list.sortedBy { it.playCount }
            SortOption.DURATION_ASC -> list.sortedBy { it.duration }
            SortOption.DURATION_DESC -> list.sortedByDescending { it.duration }
            SortOption.FAVORITES_FIRST -> list.sortedWith(compareByDescending<Song> { it.favorite }.thenBy { it.title.lowercase() })
            SortOption.RATING_HIGHEST -> list.sortedByDescending { it.rating }
            SortOption.YEAR_NEWEST -> list.sortedByDescending { it.year }
            SortOption.YEAR_OLDEST -> list.sortedBy { it.year }
            SortOption.TRACK_NUMBER -> list.sortedBy { it.trackNumber }
            SortOption.ALBUM -> list.sortedBy { it.album.lowercase() }
        }
    }

    // Playback Actions
    fun playSong(song: Song) = playbackManager.playSong(song)
    fun playQueue(songs: List<Song>, startIndex: Int = 0) = playbackManager.playQueue(songs, startIndex)
    fun togglePlayPause() = playbackManager.togglePlayPause()
    fun playNext() = playbackManager.playNext()
    fun playPrevious() = playbackManager.playPrevious()
    fun seekTo(pos: Long) = playbackManager.seekTo(pos)
    fun seekForward10s() = playbackManager.seekForward10s()
    fun seekBackward10s() = playbackManager.seekBackward10s()
    fun setPlaybackSpeed(speed: Float) {
        playbackManager.setPlaybackSpeed(speed)
        viewModelScope.launch { preferences.setPlaybackSpeed(speed) }
    }
    fun toggleShuffle() {
        val next = !playerUiState.value.isShuffleOn
        playbackManager.setShuffle(next)
        viewModelScope.launch { preferences.setShuffle(next) }
    }
    fun toggleRepeat() {
        playbackManager.toggleRepeatMode()
        viewModelScope.launch { preferences.setRepeatMode(playerUiState.value.repeatMode) }
    }

    // A-B Looper
    fun setLoopA(pos: Long) = playbackManager.abLooper.setPointA(pos)
    fun setLoopB(pos: Long) = playbackManager.abLooper.setPointB(pos)
    fun toggleLoop() = playbackManager.abLooper.toggleLoop()
    fun clearLoop() = playbackManager.abLooper.clearLoop()

    // Sleep Timer
    fun startSleepTimer(minutes: Int) = playbackManager.sleepTimer.startTimer(minutes)
    fun extendSleepTimer(minutes: Int = 5) = playbackManager.sleepTimer.extendTimer(minutes)
    fun cancelSleepTimer() = playbackManager.sleepTimer.cancelTimer()

    // Equalizer & Audio Effects
    fun setEqualizerEnabled(enabled: Boolean) {
        val curr = playerUiState.value.audioEffects
        playbackManager.updateAudioEffects(curr.copy(equalizerEnabled = enabled))
    }

    fun setBandLevel(bandIndex: Int, levelMillibels: Int) {
        val curr = playerUiState.value.audioEffects
        val newLevels = curr.bandLevels.toMutableList()
        if (bandIndex in newLevels.indices) {
            newLevels[bandIndex] = levelMillibels
            playbackManager.updateAudioEffects(curr.copy(bandLevels = newLevels, currentPresetName = "Custom"))
        }
    }

    fun setBassBoost(enabled: Boolean, strength: Int) {
        val curr = playerUiState.value.audioEffects
        playbackManager.updateAudioEffects(curr.copy(bassBoostEnabled = enabled, bassBoostStrength = strength))
        viewModelScope.launch { preferences.setBassBoostStrength(strength) }
    }

    fun setBassBoostEnabled(enabled: Boolean) {
        val curr = playerUiState.value.audioEffects
        playbackManager.updateAudioEffects(curr.copy(bassBoostEnabled = enabled))
    }

    fun setBassBoostStrength(strength: Int) {
        val curr = playerUiState.value.audioEffects
        setBassBoost(curr.bassBoostEnabled, strength)
    }

    fun setVirtualizer(enabled: Boolean, strength: Int) {
        val curr = playerUiState.value.audioEffects
        playbackManager.updateAudioEffects(curr.copy(virtualizerEnabled = enabled, virtualizerStrength = strength))
        viewModelScope.launch { preferences.setVirtualizerStrength(strength) }
    }

    fun setVirtualizerEnabled(enabled: Boolean) {
        val curr = playerUiState.value.audioEffects
        playbackManager.updateAudioEffects(curr.copy(virtualizerEnabled = enabled))
    }

    fun setVirtualizerStrength(strength: Int) {
        val curr = playerUiState.value.audioEffects
        setVirtualizer(curr.virtualizerEnabled, strength)
    }

    fun setLoudnessEnhancerEnabled(enabled: Boolean) {
        val curr = playerUiState.value.audioEffects
        playbackManager.updateAudioEffects(curr.copy(loudnessEnhancerEnabled = enabled))
    }

    fun setStereoBalance(balance: Float) {
        playbackManager.setStereoBalance(balance)
        viewModelScope.launch { preferences.setStereoBalance(balance) }
    }

    fun setMonoAudio(mono: Boolean) {
        playbackManager.setMonoAudio(mono)
        viewModelScope.launch { preferences.setMonoAudio(mono) }
    }

    fun applyPreset(preset: EqualizerPreset) {
        val levels = listOf(preset.band60Hz, preset.band250Hz, preset.band1kHz, preset.band4kHz, preset.band12kHz)
        val newSettings = AudioEffectSettings(
            equalizerEnabled = true,
            currentPresetName = preset.name,
            bandLevels = levels,
            bassBoostEnabled = preset.bassBoost > 0,
            bassBoostStrength = preset.bassBoost,
            virtualizerEnabled = preset.virtualizer > 0,
            virtualizerStrength = preset.virtualizer,
            loudnessEnhancerEnabled = preset.loudnessGain > 0,
            loudnessGain = preset.loudnessGain
        )
        playbackManager.updateAudioEffects(newSettings)
        viewModelScope.launch { preferences.setCurrentPresetName(preset.name) }
    }

    fun saveCustomPreset(name: String) = viewModelScope.launch {
        val curr = playerUiState.value.audioEffects
        val preset = EqualizerPreset(
            name = name,
            isCustom = true,
            band60Hz = curr.bandLevels.getOrElse(0) { 0 },
            band250Hz = curr.bandLevels.getOrElse(1) { 0 },
            band1kHz = curr.bandLevels.getOrElse(2) { 0 },
            band4kHz = curr.bandLevels.getOrElse(3) { 0 },
            band12kHz = curr.bandLevels.getOrElse(4) { 0 },
            bassBoost = curr.bassBoostStrength,
            virtualizer = curr.virtualizerStrength
        )
        repository.insertPreset(preset)
    }

    fun deletePreset(presetId: Long) = viewModelScope.launch {
        repository.deletePreset(presetId)
    }

    // Song CRUD & Batch
    fun toggleFavorite(song: Song) = viewModelScope.launch {
        repository.toggleFavorite(song.id, !song.favorite)
    }

    fun updateSong(song: Song) = viewModelScope.launch {
        repository.updateSong(song)
    }

    fun addSong(song: Song) = viewModelScope.launch {
        repository.insertSong(song)
    }

    fun generatePracticeTrack(title: String, durationSec: Int) = viewModelScope.launch {
        repository.generatePracticeAudio(title, durationSec)
    }

    fun deleteSong(song: Song, deletePhysicalFile: Boolean) = viewModelScope.launch {
        repository.deleteSong(song, deletePhysicalFile)
    }

    // Batch Operations
    fun toggleSelectSong(songId: Long) {
        val current = selectedSongIds.value.toMutableSet()
        if (current.contains(songId)) {
            current.remove(songId)
        } else {
            current.add(songId)
        }
        selectedSongIds.value = current
    }

    fun selectAllSongs(songs: List<Song>) {
        selectedSongIds.value = songs.map { it.id }.toSet()
    }

    fun clearSelection() {
        selectedSongIds.value = emptySet()
    }

    fun batchFavorite(favorite: Boolean) = viewModelScope.launch {
        repository.batchSetFavorite(selectedSongIds.value.toList(), favorite)
        clearSelection()
    }

    fun batchSetGenre(genre: String) = viewModelScope.launch {
        repository.batchSetGenre(selectedSongIds.value.toList(), genre)
        clearSelection()
    }

    fun batchSetRating(rating: Float) = viewModelScope.launch {
        repository.batchSetRating(selectedSongIds.value.toList(), rating)
        clearSelection()
    }

    fun batchMarkPlayed(played: Boolean) = viewModelScope.launch {
        repository.batchMarkPlayed(selectedSongIds.value.toList(), played)
        clearSelection()
    }

    fun batchDelete(deletePhysicalFiles: Boolean) = viewModelScope.launch {
        val ids = selectedSongIds.value.toList()
        val songs = repository.getSongsByIdsDirect(ids)
        repository.deleteSongs(songs, deletePhysicalFiles)
        clearSelection()
    }

    // Playlists
    fun createPlaylist(name: String, description: String = "", coverUri: String? = null) = viewModelScope.launch {
        val p = Playlist(name = name, description = description, coverUri = coverUri)
        repository.insertPlaylist(p)
    }

    fun updatePlaylist(playlist: Playlist) = viewModelScope.launch {
        repository.updatePlaylist(playlist)
    }

    fun deletePlaylist(playlistId: Long) = viewModelScope.launch {
        repository.deletePlaylist(playlistId)
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) = viewModelScope.launch {
        repository.addSongToPlaylist(playlistId, songId)
    }

    fun batchAddToPlaylist(playlistId: Long) = viewModelScope.launch {
        repository.addSongsToPlaylist(playlistId, selectedSongIds.value.toList())
        clearSelection()
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) = viewModelScope.launch {
        repository.removeSongFromPlaylist(playlistId, songId)
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> = repository.getSongsForPlaylist(playlistId)
    fun getSongsForAlbum(album: String): Flow<List<Song>> = repository.getSongsByAlbum(album)
    fun getSongsForArtist(artist: String): Flow<List<Song>> = repository.getSongsByArtist(artist)

    // Lyrics
    fun getLyricsForSong(songId: Long): Flow<List<LyricsLine>> = repository.getLyricsForSong(songId)
    fun saveLyrics(songId: Long, rawLyrics: String, lines: List<LyricsLine>) = viewModelScope.launch {
        repository.saveLyrics(songId, rawLyrics, lines)
    }
    fun addLyricsLine(line: LyricsLine) = viewModelScope.launch {
        repository.insertLyricsLine(line)
    }
    fun deleteLyricsLine(id: Long) = viewModelScope.launch {
        repository.deleteLyricsLine(id)
    }

    // Scanning
    fun scanDeviceMedia() = viewModelScope.launch {
        isScanning.value = true
        scanProgress.value = 0f
        try {
            repository.scanMedia { progress ->
                scanProgress.value = progress
            }
        } finally {
            isScanning.value = false
        }
    }

    // Settings
    fun setTheme(theme: SalimTheme) = viewModelScope.launch { preferences.setAppTheme(theme) }
    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { preferences.setThemeMode(mode) }
    fun setDynamicColors(enabled: Boolean) = viewModelScope.launch { preferences.setDynamicColors(enabled) }
    fun setHighContrast(enabled: Boolean) = viewModelScope.launch { preferences.setHighContrast(enabled) }
    fun setCompactMode(enabled: Boolean) = viewModelScope.launch { preferences.setCompactMode(enabled) }
    fun setReducedMotion(enabled: Boolean) = viewModelScope.launch { preferences.setReducedMotion(enabled) }
    fun setSortOption(sort: SortOption) = viewModelScope.launch { preferences.setSortOption(sort) }
    fun setGaplessPlayback(enabled: Boolean) = viewModelScope.launch { preferences.setGaplessPlayback(enabled) }
    fun setAutoScrollLyrics(enabled: Boolean) = viewModelScope.launch { preferences.setAutoScrollLyrics(enabled) }
    fun setKeepScreenAwake(enabled: Boolean) = viewModelScope.launch { preferences.setKeepScreenAwake(enabled) }
    fun setLyricsTextSize(size: Float) = viewModelScope.launch { preferences.setLyricsTextSize(size) }
    fun setFilterGenre(genre: String?) { selectedGenreFilter.value = genre }
    fun resetSettings() = viewModelScope.launch { preferences.resetAllSettings() }

    // History
    fun clearHistory() = viewModelScope.launch { repository.clearAllHistory() }
}
