package com.example.data.backup

import android.content.Context
import android.net.Uri
import com.example.data.local.*
import com.example.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class BackupRestoreManager(
    private val context: Context,
    private val database: AppDatabase,
    private val preferencesManager: PreferencesManager
) {

    data class ImportPreview(
        val version: Int,
        val exportDate: Long,
        val songCount: Int,
        val playlistCount: Int,
        val historyCount: Int,
        val lyricsCount: Int,
        val presetCount: Int
    )

    data class ImportResult(
        val success: Boolean,
        val songsImported: Int,
        val playlistsImported: Int,
        val historyImported: Int,
        val lyricsImported: Int,
        val message: String
    )

    suspend fun exportBackupToJson(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject()
            root.put("version", 1)
            root.put("appName", "Salim Music Player")
            root.put("exportDate", System.currentTimeMillis())

            // Songs
            val songs = database.songDao().getAllSongsList()
            val songsArray = JSONArray()
            for (s in songs) {
                val obj = JSONObject().apply {
                    put("id", s.id)
                    put("title", s.title)
                    put("artist", s.artist)
                    put("album", s.album)
                    put("albumArtist", s.albumArtist)
                    put("genre", s.genre)
                    put("year", s.year)
                    put("duration", s.duration)
                    put("fileUri", s.fileUri)
                    put("artworkUri", s.artworkUri ?: "")
                    put("trackNumber", s.trackNumber)
                    put("discNumber", s.discNumber)
                    put("lyrics", s.lyrics)
                    put("rating", s.rating.toDouble())
                    put("favorite", s.favorite)
                    put("playCount", s.playCount)
                    put("lastPlayedTimestamp", s.lastPlayedTimestamp)
                    put("dateAdded", s.dateAdded)
                    put("fileSize", s.fileSize)
                    put("mimeType", s.mimeType)
                }
                songsArray.put(obj)
            }
            root.put("songs", songsArray)

            // Playlists
            val playlists = database.playlistDao().getAllPlaylistsList()
            val playlistsArray = JSONArray()
            for (p in playlists) {
                val obj = JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("description", p.description)
                    put("coverUri", p.coverUri ?: "")
                    put("gradient", p.gradient)
                    put("accentColor", p.accentColor)
                    put("createdTimestamp", p.createdTimestamp)
                    put("modifiedTimestamp", p.modifiedTimestamp)
                }
                playlistsArray.put(obj)
            }
            root.put("playlists", playlistsArray)

            // Playlist Songs
            val playlistSongs = database.playlistDao().getAllPlaylistSongsList()
            val playlistSongsArray = JSONArray()
            for (ps in playlistSongs) {
                val obj = JSONObject().apply {
                    put("playlistId", ps.playlistId)
                    put("songId", ps.songId)
                    put("position", ps.position)
                    put("addedTimestamp", ps.addedTimestamp)
                }
                playlistSongsArray.put(obj)
            }
            root.put("playlistSongs", playlistSongsArray)

            // Play History
            val histories = database.playHistoryDao().getAllHistoryList()
            val historyArray = JSONArray()
            for (h in histories) {
                val obj = JSONObject().apply {
                    put("id", h.id)
                    put("songId", h.songId)
                    put("startedTimestamp", h.startedTimestamp)
                    put("endedTimestamp", h.endedTimestamp)
                    put("playedDuration", h.playedDuration)
                    put("completed", h.completed)
                }
                historyArray.put(obj)
            }
            root.put("history", historyArray)

            // Lyrics Lines
            val lyrics = database.lyricsDao().getAllLyricsLines()
            val lyricsArray = JSONArray()
            for (l in lyrics) {
                val obj = JSONObject().apply {
                    put("id", l.id)
                    put("songId", l.songId)
                    put("timestamp", l.timestamp)
                    put("text", l.text)
                    put("position", l.position)
                }
                lyricsArray.put(obj)
            }
            root.put("lyricsLines", lyricsArray)

            // Equalizer Presets
            val presets = database.equalizerPresetDao().getAllPresetsList()
            val presetsArray = JSONArray()
            for (pr in presets) {
                val obj = JSONObject().apply {
                    put("name", pr.name)
                    put("isCustom", pr.isCustom)
                    put("band60Hz", pr.band60Hz)
                    put("band250Hz", pr.band250Hz)
                    put("band1kHz", pr.band1kHz)
                    put("band4kHz", pr.band4kHz)
                    put("band12kHz", pr.band12kHz)
                    put("bassBoost", pr.bassBoost)
                    put("virtualizer", pr.virtualizer)
                    put("loudnessGain", pr.loudnessGain)
                }
                presetsArray.put(obj)
            }
            root.put("presets", presetsArray)

            context.contentResolver.openOutputStream(uri)?.use { os ->
                OutputStreamWriter(os).use { writer ->
                    writer.write(root.toString(2))
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun parsePreview(uri: Uri): ImportPreview? = withContext(Dispatchers.IO) {
        try {
            val jsonString = readUriToString(uri) ?: return@withContext null
            val root = JSONObject(jsonString)
            ImportPreview(
                version = root.optInt("version", 1),
                exportDate = root.optLong("exportDate", System.currentTimeMillis()),
                songCount = root.optJSONArray("songs")?.length() ?: 0,
                playlistCount = root.optJSONArray("playlists")?.length() ?: 0,
                historyCount = root.optJSONArray("history")?.length() ?: 0,
                lyricsCount = root.optJSONArray("lyricsLines")?.length() ?: 0,
                presetCount = root.optJSONArray("presets")?.length() ?: 0
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun importBackupFromJson(uri: Uri, replaceExisting: Boolean): ImportResult = withContext(Dispatchers.IO) {
        try {
            val jsonString = readUriToString(uri) ?: return@withContext ImportResult(false, 0, 0, 0, 0, "Cannot read file")
            val root = JSONObject(jsonString)

            if (replaceExisting) {
                database.clearAllTables()
            }

            var importedSongs = 0
            var importedPlaylists = 0
            var importedHistory = 0
            var importedLyrics = 0

            // 1. Songs
            val songsArray = root.optJSONArray("songs")
            if (songsArray != null) {
                val songsList = mutableListOf<Song>()
                for (i in 0 until songsArray.length()) {
                    val obj = songsArray.getJSONObject(i)
                    val song = Song(
                        id = if (replaceExisting) obj.optLong("id") else 0L,
                        title = obj.optString("title", "Unknown"),
                        artist = obj.optString("artist", "Unknown Artist"),
                        album = obj.optString("album", "Unknown Album"),
                        albumArtist = obj.optString("albumArtist", "Unknown Artist"),
                        genre = obj.optString("genre", "All"),
                        year = obj.optInt("year", 0),
                        duration = obj.optLong("duration", 0L),
                        fileUri = obj.optString("fileUri"),
                        artworkUri = obj.optString("artworkUri").takeIf { it.isNotBlank() },
                        trackNumber = obj.optInt("trackNumber", 0),
                        discNumber = obj.optInt("discNumber", 1),
                        lyrics = obj.optString("lyrics", ""),
                        rating = obj.optDouble("rating", 0.0).toFloat(),
                        favorite = obj.optBoolean("favorite", false),
                        playCount = obj.optInt("playCount", 0),
                        lastPlayedTimestamp = obj.optLong("lastPlayedTimestamp", 0L),
                        dateAdded = obj.optLong("dateAdded", System.currentTimeMillis()),
                        fileSize = obj.optLong("fileSize", 0L),
                        mimeType = obj.optString("mimeType", "audio/mpeg")
                    )
                    songsList.add(song)
                }
                database.songDao().insertSongs(songsList)
                importedSongs = songsList.size
            }

            // 2. Playlists
            val playlistsArray = root.optJSONArray("playlists")
            if (playlistsArray != null) {
                val playlistList = mutableListOf<Playlist>()
                for (i in 0 until playlistsArray.length()) {
                    val obj = playlistsArray.getJSONObject(i)
                    playlistList.add(
                        Playlist(
                            id = if (replaceExisting) obj.optLong("id") else 0L,
                            name = obj.optString("name", "Playlist"),
                            description = obj.optString("description", ""),
                            coverUri = obj.optString("coverUri").takeIf { it.isNotBlank() },
                            gradient = obj.optString("gradient", "emerald_slate"),
                            accentColor = obj.optLong("accentColor", 0xFF10B981),
                            createdTimestamp = obj.optLong("createdTimestamp", System.currentTimeMillis()),
                            modifiedTimestamp = obj.optLong("modifiedTimestamp", System.currentTimeMillis())
                        )
                    )
                }
                database.playlistDao().insertPlaylists(playlistList)
                importedPlaylists = playlistList.size
            }

            // 3. Playlist Songs
            val psArray = root.optJSONArray("playlistSongs")
            if (psArray != null) {
                val psList = mutableListOf<PlaylistSong>()
                for (i in 0 until psArray.length()) {
                    val obj = psArray.getJSONObject(i)
                    psList.add(
                        PlaylistSong(
                            playlistId = obj.getLong("playlistId"),
                            songId = obj.getLong("songId"),
                            position = obj.getInt("position"),
                            addedTimestamp = obj.optLong("addedTimestamp", System.currentTimeMillis())
                        )
                    )
                }
                database.playlistDao().insertPlaylistSongs(psList)
            }

            // 4. History
            val histArray = root.optJSONArray("history")
            if (histArray != null) {
                val histList = mutableListOf<PlayHistory>()
                for (i in 0 until histArray.length()) {
                    val obj = histArray.getJSONObject(i)
                    histList.add(
                        PlayHistory(
                            songId = obj.getLong("songId"),
                            startedTimestamp = obj.optLong("startedTimestamp"),
                            endedTimestamp = obj.optLong("endedTimestamp"),
                            playedDuration = obj.optLong("playedDuration"),
                            completed = obj.optBoolean("completed", false)
                        )
                    )
                }
                database.playHistoryDao().insertHistories(histList)
                importedHistory = histList.size
            }

            // 5. Lyrics
            val lyricsArray = root.optJSONArray("lyricsLines")
            if (lyricsArray != null) {
                val linesList = mutableListOf<LyricsLine>()
                for (i in 0 until lyricsArray.length()) {
                    val obj = lyricsArray.getJSONObject(i)
                    linesList.add(
                        LyricsLine(
                            songId = obj.getLong("songId"),
                            timestamp = obj.getLong("timestamp"),
                            text = obj.getString("text"),
                            position = obj.optInt("position", i)
                        )
                    )
                }
                database.lyricsDao().insertLines(linesList)
                importedLyrics = linesList.size
            }

            ImportResult(
                success = true,
                songsImported = importedSongs,
                playlistsImported = importedPlaylists,
                historyImported = importedHistory,
                lyricsImported = importedLyrics,
                message = "Backup successfully restored"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult(false, 0, 0, 0, 0, "Restore failed: ${e.localizedMessage}")
        }
    }

    private fun readUriToString(uri: Uri): String? {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
        }
    }
}
