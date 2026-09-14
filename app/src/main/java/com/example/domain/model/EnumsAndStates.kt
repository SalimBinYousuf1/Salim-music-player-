package com.example.domain.model

enum class SortOption(val displayName: String) {
    TITLE_AZ("Title (A-Z)"),
    TITLE_ZA("Title (Z-A)"),
    RECENTLY_ADDED("Recently Added"),
    RECENTLY_PLAYED("Recently Played"),
    MOST_PLAYED("Most Played"),
    LEAST_PLAYED("Least Played"),
    DURATION_ASC("Duration (Shortest)"),
    DURATION_DESC("Duration (Longest)"),
    FAVORITES_FIRST("Favorites First"),
    RATING_HIGHEST("Rating (Highest)"),
    YEAR_NEWEST("Year (Newest)"),
    YEAR_OLDEST("Year (Oldest)"),
    TRACK_NUMBER("Track Number"),
    ALBUM("Album Order")
}

enum class VisualizerMode(val displayName: String) {
    FREQUENCY_BARS("Frequency Bars"),
    OSCILLOSCOPE("Oscilloscope"),
    RADIAL_PULSE("Radial Pulse"),
    BEAT_RINGS("Beat Rings"),
    CYBER_PARTICLES("Cyber Particles"),
    CIRCULAR_BARS("Circular Bars"),
    WAVE_HORIZON("Wave Horizon"),
    NEON_HEARTBEAT("Neon Heartbeat")
}

enum class SalimTheme(val displayName: String) {
    DARK_SLATE("Dark Slate"),
    LIGHT_SLATE("Light Slate"),
    MIDNIGHT_VIOLET("Midnight Violet"),
    OLED_PITCH_BLACK("OLED Pitch Black"),
    SUNSET_AMBER("Sunset Amber"),
    FOREST_GREEN("Forest Green"),
    OCEAN_BLUE("Ocean Blue"),
    AURORA_GLASS("Aurora Glass"),
    CYBER_NEON("Cyber Neon"),
    ROSE_QUARTZ("Rose Quartz Glass"),
    LIQUID_EMERALD("Liquid Emerald"),
    SUNSET_HORIZON("Sunset Horizon")
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class PlaybackQueueItem(
    val song: Song,
    val queueId: Long = System.nanoTime()
)

data class AbLoopState(
    val isEnabled: Boolean = false,
    val pointA: Long? = null, // ms
    val pointB: Long? = null  // ms
)

data class SleepTimerState(
    val isActive: Boolean = false,
    val totalSeconds: Long = 0L,
    val remainingSeconds: Long = 0L,
    val fadeOutBeforePause: Boolean = true
)

data class AudioEffectSettings(
    val equalizerEnabled: Boolean = true,
    val currentPresetName: String = "Flat",
    val bandLevels: List<Int> = listOf(0, 0, 0, 0, 0), // 60Hz, 250Hz, 1kHz, 4kHz, 12kHz
    val bassBoostEnabled: Boolean = false,
    val bassBoostStrength: Int = 0, // 0 - 1000
    val virtualizerEnabled: Boolean = false,
    val virtualizerStrength: Int = 0, // 0 - 1000
    val stereoBalance: Float = 0f, // -1.0 (left) to 1.0 (right)
    val monoAudio: Boolean = false,
    val loudnessEnhancerEnabled: Boolean = false,
    val loudnessGain: Int = 0
)

data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val bufferedPosition: Long = 0L,
    val isBuffering: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val isShuffleOn: Boolean = false,
    val repeatMode: Int = 0, // 0: off, 1: repeat one, 2: repeat all
    val queue: List<Song> = emptyList(),
    val queueIndex: Int = -1,
    val abLoop: AbLoopState = AbLoopState(),
    val sleepTimer: SleepTimerState = SleepTimerState(),
    val audioEffects: AudioEffectSettings = AudioEffectSettings(),
    val isVisualizerActive: Boolean = false
)
