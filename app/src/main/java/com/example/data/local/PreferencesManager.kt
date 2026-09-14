package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.SalimTheme
import com.example.domain.model.SortOption
import com.example.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "salim_settings")

class PreferencesManager(private val context: Context) {

    private object Keys {
        val LAST_SONG_ID = longPreferencesKey("last_song_id")
        val LAST_POSITION = longPreferencesKey("last_position")
        val SHUFFLE = booleanPreferencesKey("shuffle")
        val REPEAT_MODE = intPreferencesKey("repeat_mode")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val APP_THEME = stringPreferencesKey("app_theme")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val COMPACT_MODE = booleanPreferencesKey("compact_mode")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val KEEP_SCREEN_AWAKE = booleanPreferencesKey("keep_screen_awake")
        val SORT_OPTION = stringPreferencesKey("sort_option")
        val SCAN_ON_LAUNCH = booleanPreferencesKey("scan_on_launch")
        val GAPLESS_PLAYBACK = booleanPreferencesKey("gapless_playback")
        val AUTO_SCROLL_LYRICS = booleanPreferencesKey("auto_scroll_lyrics")
        val HIGHLIGHT_ACTIVE_LINE = booleanPreferencesKey("highlight_active_line")
        val LYRICS_TEXT_SIZE = floatPreferencesKey("lyrics_text_size")
        val BASS_BOOST_STRENGTH = intPreferencesKey("bass_boost_strength")
        val VIRTUALIZER_STRENGTH = intPreferencesKey("virtualizer_strength")
        val STEREO_BALANCE = floatPreferencesKey("stereo_balance")
        val MONO_AUDIO = booleanPreferencesKey("mono_audio")
        val EQUALIZER_PRESET = stringPreferencesKey("equalizer_preset")
    }

    val lastSongId: Flow<Long> = context.dataStore.data.safeCatch().map { it[Keys.LAST_SONG_ID] ?: -1L }
    val lastPosition: Flow<Long> = context.dataStore.data.safeCatch().map { it[Keys.LAST_POSITION] ?: 0L }
    val isShuffle: Flow<Boolean> = context.dataStore.data.safeCatch().map { it[Keys.SHUFFLE] ?: false }
    val repeatMode: Flow<Int> = context.dataStore.data.safeCatch().map { it[Keys.REPEAT_MODE] ?: 0 }
    val playbackSpeed: Flow<Float> = context.dataStore.data.safeCatch().map { it[Keys.PLAYBACK_SPEED] ?: 1.0f }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.safeCatch().map {
        try {
            ThemeMode.valueOf(it[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    val appTheme: Flow<SalimTheme> = context.dataStore.data.safeCatch().map {
        try {
            SalimTheme.valueOf(it[Keys.APP_THEME] ?: SalimTheme.DARK_SLATE.name)
        } catch (e: Exception) {
            SalimTheme.DARK_SLATE
        }
    }

    val dynamicColors: Flow<Boolean> = context.dataStore.data.safeCatch().map { it[Keys.DYNAMIC_COLORS] ?: false }
    val highContrast: Flow<Boolean> = context.dataStore.data.safeCatch().map { it[Keys.HIGH_CONTRAST] ?: false }
    val compactMode: Flow<Boolean> = context.dataStore.data.safeCatch().map { it[Keys.COMPACT_MODE] ?: false }
    val reducedMotion: Flow<Boolean> = context.dataStore.data.safeCatch().map { it[Keys.REDUCED_MOTION] ?: false }
    val keepScreenAwake: Flow<Boolean> = context.dataStore.data.safeCatch().map { it[Keys.KEEP_SCREEN_AWAKE] ?: false }

    val sortOption: Flow<SortOption> = context.dataStore.data.safeCatch().map {
        try {
            SortOption.valueOf(it[Keys.SORT_OPTION] ?: SortOption.TITLE_AZ.name)
        } catch (e: Exception) {
            SortOption.TITLE_AZ
        }
    }

    val scanOnLaunch: Flow<Boolean> = context.dataStore.data.safeCatch().map { it[Keys.SCAN_ON_LAUNCH] ?: true }
    val gaplessPlayback: Flow<Boolean> = context.dataStore.data.safeCatch().map { it[Keys.GAPLESS_PLAYBACK] ?: true }
    val autoScrollLyrics: Flow<Boolean> = context.dataStore.data.safeCatch().map { it[Keys.AUTO_SCROLL_LYRICS] ?: true }
    val highlightActiveLine: Flow<Boolean> = context.dataStore.data.safeCatch().map { it[Keys.HIGHLIGHT_ACTIVE_LINE] ?: true }
    val lyricsTextSize: Flow<Float> = context.dataStore.data.safeCatch().map { it[Keys.LYRICS_TEXT_SIZE] ?: 18f }

    val bassBoostStrength: Flow<Int> = context.dataStore.data.safeCatch().map { it[Keys.BASS_BOOST_STRENGTH] ?: 0 }
    val virtualizerStrength: Flow<Int> = context.dataStore.data.safeCatch().map { it[Keys.VIRTUALIZER_STRENGTH] ?: 0 }
    val stereoBalance: Flow<Float> = context.dataStore.data.safeCatch().map { it[Keys.STEREO_BALANCE] ?: 0f }
    val monoAudio: Flow<Boolean> = context.dataStore.data.safeCatch().map { it[Keys.MONO_AUDIO] ?: false }
    val currentPresetName: Flow<String> = context.dataStore.data.safeCatch().map { it[Keys.EQUALIZER_PRESET] ?: "Flat" }

    suspend fun setLastPlayback(songId: Long, position: Long) {
        context.dataStore.edit {
            it[Keys.LAST_SONG_ID] = songId
            it[Keys.LAST_POSITION] = position
        }
    }

    suspend fun setShuffle(shuffle: Boolean) {
        context.dataStore.edit { it[Keys.SHUFFLE] = shuffle }
    }

    suspend fun setRepeatMode(mode: Int) {
        context.dataStore.edit { it[Keys.REPEAT_MODE] = mode }
    }

    suspend fun setPlaybackSpeed(speed: Float) {
        context.dataStore.edit { it[Keys.PLAYBACK_SPEED] = speed }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setAppTheme(theme: SalimTheme) {
        context.dataStore.edit { it[Keys.APP_THEME] = theme.name }
    }

    suspend fun setDynamicColors(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLORS] = enabled }
    }

    suspend fun setHighContrast(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HIGH_CONTRAST] = enabled }
    }

    suspend fun setCompactMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.COMPACT_MODE] = enabled }
    }

    suspend fun setReducedMotion(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REDUCED_MOTION] = enabled }
    }

    suspend fun setKeepScreenAwake(enabled: Boolean) {
        context.dataStore.edit { it[Keys.KEEP_SCREEN_AWAKE] = enabled }
    }

    suspend fun setSortOption(sort: SortOption) {
        context.dataStore.edit { it[Keys.SORT_OPTION] = sort.name }
    }

    suspend fun setScanOnLaunch(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SCAN_ON_LAUNCH] = enabled }
    }

    suspend fun setGaplessPlayback(enabled: Boolean) {
        context.dataStore.edit { it[Keys.GAPLESS_PLAYBACK] = enabled }
    }

    suspend fun setAutoScrollLyrics(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_SCROLL_LYRICS] = enabled }
    }

    suspend fun setHighlightActiveLine(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HIGHLIGHT_ACTIVE_LINE] = enabled }
    }

    suspend fun setLyricsTextSize(size: Float) {
        context.dataStore.edit { it[Keys.LYRICS_TEXT_SIZE] = size }
    }

    suspend fun setBassBoostStrength(strength: Int) {
        context.dataStore.edit { it[Keys.BASS_BOOST_STRENGTH] = strength }
    }

    suspend fun setVirtualizerStrength(strength: Int) {
        context.dataStore.edit { it[Keys.VIRTUALIZER_STRENGTH] = strength }
    }

    suspend fun setStereoBalance(balance: Float) {
        context.dataStore.edit { it[Keys.STEREO_BALANCE] = balance }
    }

    suspend fun setMonoAudio(mono: Boolean) {
        context.dataStore.edit { it[Keys.MONO_AUDIO] = mono }
    }

    suspend fun setCurrentPresetName(preset: String) {
        context.dataStore.edit { it[Keys.EQUALIZER_PRESET] = preset }
    }

    suspend fun resetAllSettings() {
        context.dataStore.edit { it.clear() }
    }
}

private fun Flow<Preferences>.safeCatch(): Flow<Preferences> = catch { exception ->
    if (exception is IOException) {
        emit(emptyPreferences())
    } else {
        throw exception
    }
}
