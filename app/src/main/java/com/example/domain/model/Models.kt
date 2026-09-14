package com.example.domain.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["fileUri"], unique = true),
        Index(value = ["artist"]),
        Index(value = ["album"]),
        Index(value = ["genre"]),
        Index(value = ["favorite"])
    ]
)
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String = "Unknown Artist",
    val album: String = "Unknown Album",
    val albumArtist: String = "Unknown Artist",
    val genre: String = "All",
    val year: Int = 0,
    val duration: Long = 0L, // ms
    val fileUri: String,
    val artworkUri: String? = null,
    val trackNumber: Int = 0,
    val discNumber: Int = 0,
    val lyrics: String = "",
    val rating: Float = 0f, // 0 to 5
    val favorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis(),
    val lastModifiedTimestamp: Long = System.currentTimeMillis(),
    val fileSize: Long = 0L,
    val mimeType: String = "audio/mpeg"
)

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val coverUri: String? = null,
    val gradient: String = "emerald_slate", // preset key
    val accentColor: Long = 0xFF10B981,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val modifiedTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId", "position"],
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["playlistId"]), Index(value = ["songId"])]
)
data class PlaylistSong(
    val playlistId: Long,
    val songId: Long,
    val position: Int,
    val addedTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "play_history",
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["songId"]), Index(value = ["startedTimestamp"])]
)
data class PlayHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: Long,
    val startedTimestamp: Long = System.currentTimeMillis(),
    val endedTimestamp: Long = System.currentTimeMillis(),
    val playedDuration: Long = 0L,
    val completed: Boolean = false,
    val sessionId: String = ""
)

@Entity(
    tableName = "lyrics_lines",
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["songId"]), Index(value = ["timestamp"])]
)
data class LyricsLine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: Long,
    val timestamp: Long, // milliseconds
    val text: String,
    val position: Int = 0
)

@Entity(tableName = "equalizer_presets")
data class EqualizerPreset(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isCustom: Boolean = false,
    val band60Hz: Int = 0,   // millibels (-1500 to +1500)
    val band250Hz: Int = 0,
    val band1kHz: Int = 0,
    val band4kHz: Int = 0,
    val band12kHz: Int = 0,
    val bassBoost: Int = 0,      // 0 to 1000
    val virtualizer: Int = 0,    // 0 to 1000
    val loudnessGain: Int = 0    // millibels
)
