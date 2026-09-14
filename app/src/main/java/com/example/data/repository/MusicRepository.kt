package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.*
import com.example.data.mediastore.AudioTrackGenerator
import com.example.data.mediastore.MediaStoreAudioScanner
import com.example.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.io.File

class MusicRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val preferencesManager: PreferencesManager
) {
    private val songDao = database.songDao()
    private val playlistDao = database.playlistDao()
    private val historyDao = database.playHistoryDao()
    private val lyricsDao = database.lyricsDao()
    private val presetDao = database.equalizerPresetDao()
    private val scanner = MediaStoreAudioScanner(context, songDao)

    // Songs
    fun getAllSongs(): Flow<List<Song>> = songDao.getAllSongs()
    fun getSongById(id: Long): Flow<Song?> = songDao.getSongById(id)
    suspend fun getSongByIdDirect(id: Long): Song? = songDao.getSongByIdDirect(id)
    suspend fun getSongsByIdsDirect(ids: List<Long>): List<Song> = songDao.getSongsByIdsDirect(ids)
    fun getFavorites(): Flow<List<Song>> = songDao.getFavorites()
    fun getRecentlyPlayed(limit: Int = 100): Flow<List<Song>> = songDao.getRecentlyPlayed(limit)
    fun getMostPlayed(limit: Int = 100): Flow<List<Song>> = songDao.getMostPlayed(limit)
    fun getSongsByAlbum(album: String): Flow<List<Song>> = songDao.getSongsByAlbum(album)
    fun getSongsByArtist(artist: String): Flow<List<Song>> = songDao.getSongsByArtist(artist)
    fun getSongsByGenre(genre: String): Flow<List<Song>> = songDao.getSongsByGenre(genre)
    fun searchSongs(query: String): Flow<List<Song>> = songDao.searchSongs(query)
    fun getDistinctGenres(): Flow<List<String>> = songDao.getDistinctGenres()
    fun getDistinctAlbums(): Flow<List<String>> = songDao.getDistinctAlbums()
    fun getDistinctArtists(): Flow<List<String>> = songDao.getDistinctArtists()
    fun getSongsCount(): Flow<Int> = songDao.getSongsCount()

    suspend fun insertSong(song: Song): Long = songDao.insertSong(song)
    suspend fun updateSong(song: Song) = songDao.updateSong(song)
    suspend fun toggleFavorite(id: Long, favorite: Boolean) = songDao.updateFavorite(id, favorite)
    suspend fun incrementPlayCount(id: Long, timestamp: Long = System.currentTimeMillis()) =
        songDao.incrementPlayCount(id, timestamp)
    suspend fun updateRating(id: Long, rating: Float) = songDao.updateRating(id, rating)

    suspend fun deleteSong(song: Song, deletePhysicalFile: Boolean = false) {
        if (deletePhysicalFile) {
            try {
                val uri = Uri.parse(song.fileUri)
                if (uri.scheme == "file") {
                    File(uri.path ?: "").delete()
                } else {
                    context.contentResolver.delete(uri, null, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        songDao.deleteSong(song)
    }

    suspend fun deleteSongs(songs: List<Song>, deletePhysicalFiles: Boolean = false) {
        val ids = songs.map { it.id }
        if (deletePhysicalFiles) {
            for (song in songs) {
                try {
                    val uri = Uri.parse(song.fileUri)
                    if (uri.scheme == "file") {
                        File(uri.path ?: "").delete()
                    } else {
                        context.contentResolver.delete(uri, null, null)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        songDao.deleteSongsByIds(ids)
    }

    // Batch Operations
    suspend fun batchSetFavorite(ids: List<Long>, favorite: Boolean) = songDao.batchUpdateFavorite(ids, favorite)
    suspend fun batchSetGenre(ids: List<Long>, genre: String) = songDao.batchUpdateGenre(ids, genre)
    suspend fun batchSetRating(ids: List<Long>, rating: Float) = songDao.batchUpdateRating(ids, rating)
    suspend fun batchMarkPlayed(ids: List<Long>, played: Boolean) {
        songDao.batchUpdatePlayCount(ids, if (played) 1 else 0)
    }

    // Playlists
    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()
    fun getPlaylistById(id: Long): Flow<Playlist?> = playlistDao.getPlaylistById(id)
    suspend fun getPlaylistByIdDirect(id: Long): Playlist? = playlistDao.getPlaylistByIdDirect(id)
    suspend fun insertPlaylist(playlist: Playlist): Long = playlistDao.insertPlaylist(playlist)
    suspend fun updatePlaylist(playlist: Playlist) = playlistDao.updatePlaylist(playlist)
    suspend fun deletePlaylist(playlistId: Long) = playlistDao.deletePlaylistById(playlistId)

    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> = playlistDao.getSongsForPlaylist(playlistId)
    suspend fun getSongsForPlaylistDirect(playlistId: Long): List<Song> = playlistDao.getSongsForPlaylistDirect(playlistId)
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val maxPos = playlistDao.getMaxPosition(playlistId) ?: -1
        playlistDao.insertPlaylistSong(PlaylistSong(playlistId, songId, maxPos + 1))
    }
    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        var pos = (playlistDao.getMaxPosition(playlistId) ?: -1) + 1
        val items = songIds.map { id -> PlaylistSong(playlistId, id, pos++) }
        playlistDao.insertPlaylistSongs(items)
    }
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) =
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    suspend fun removeSongsFromPlaylist(playlistId: Long, songIds: List<Long>) =
        playlistDao.removeSongsFromPlaylist(playlistId, songIds)
    suspend fun clearPlaylist(playlistId: Long) = playlistDao.clearPlaylist(playlistId)

    // Play History
    fun getAllHistory(limit: Int = 200): Flow<List<PlayHistory>> = historyDao.getAllHistory(limit)
    suspend fun logHistory(playHistory: PlayHistory) = historyDao.insertHistory(playHistory)
    suspend fun deleteHistory(id: Long) = historyDao.deleteHistoryById(id)
    suspend fun clearAllHistory() = historyDao.clearAllHistory()

    // Lyrics
    fun getLyricsForSong(songId: Long): Flow<List<LyricsLine>> = lyricsDao.getLinesForSong(songId)
    suspend fun getLyricsForSongDirect(songId: Long): List<LyricsLine> = lyricsDao.getLinesForSongDirect(songId)
    suspend fun saveLyrics(songId: Long, rawLyrics: String, lines: List<LyricsLine>) {
        val song = songDao.getSongByIdDirect(songId)
        if (song != null) {
            songDao.updateSong(song.copy(lyrics = rawLyrics))
        }
        lyricsDao.deleteLinesForSong(songId)
        if (lines.isNotEmpty()) {
            lyricsDao.insertLines(lines)
        }
    }
    suspend fun insertLyricsLine(line: LyricsLine) = lyricsDao.insertLine(line)
    suspend fun updateLyricsLine(line: LyricsLine) = lyricsDao.updateLine(line)
    suspend fun deleteLyricsLine(id: Long) = lyricsDao.deleteLineById(id)

    // Presets
    fun getAllPresets(): Flow<List<EqualizerPreset>> = presetDao.getAllPresets()
    suspend fun insertPreset(preset: EqualizerPreset): Long = presetDao.insertPreset(preset)
    suspend fun deletePreset(presetId: Long) = presetDao.deletePresetById(presetId)

    // MediaStore Scanner & Practice Generator
    suspend fun scanMedia(onProgress: (Float) -> Unit = {}) = scanner.scanDeviceAudio(onProgress)

    suspend fun generatePracticeAudio(title: String, durationSec: Int): Song {
        val file = AudioTrackGenerator.generatePracticeTrack(context, title, durationSec)
        val song = Song(
            title = title,
            artist = "Salim Practice Lab",
            album = "Native Audio Generator",
            genre = "Acoustic",
            year = 2026,
            duration = durationSec * 1000L,
            fileUri = Uri.fromFile(file).toString(),
            fileSize = file.length(),
            mimeType = "audio/wav",
            favorite = false,
            rating = 5f
        )
        val id = songDao.insertSong(song)
        return song.copy(id = id)
    }
}
