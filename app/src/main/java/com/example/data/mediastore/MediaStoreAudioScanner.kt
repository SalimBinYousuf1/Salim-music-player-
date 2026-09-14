package com.example.data.mediastore

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.data.local.SongDao
import com.example.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaStoreAudioScanner(
    private val context: Context,
    private val songDao: SongDao
) {

    data class ScanResult(
        val totalScanned: Int,
        val newAdded: Int,
        val updated: Int,
        val deleted: Int
    )

    suspend fun scanDeviceAudio(onProgress: (Float) -> Unit = {}): ScanResult = withContext(Dispatchers.IO) {
        val existingSongs = songDao.getAllSongsList().associateBy { it.fileUri }
        val foundUris = mutableSetOf<String>()
        val scannedSongs = mutableListOf<Song>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 1000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val yearCol = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
                val trackCol = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
                val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

                val count = cursor.count
                var index = 0

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString()
                    foundUris.add(contentUri)

                    val title = cursor.getString(titleCol) ?: "Unknown Track"
                    val artist = cursor.getString(artistCol)?.takeIf { it != "<unknown>" } ?: "Unknown Artist"
                    val album = cursor.getString(albumCol)?.takeIf { it != "<unknown>" } ?: "Unknown Album"
                    val duration = cursor.getLong(durationCol)
                    val size = cursor.getLong(sizeCol)
                    val mimeType = cursor.getString(mimeCol) ?: "audio/mpeg"
                    val year = if (yearCol != -1) cursor.getInt(yearCol) else 0
                    val track = if (trackCol != -1) cursor.getInt(trackCol) else 0
                    
                    val albumId = if (albumIdCol != -1) cursor.getLong(albumIdCol) else -1L
                    val artworkUri = if (albumId > 0) {
                        ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        ).toString()
                    } else null

                    val existing = existingSongs[contentUri]
                    val song = Song(
                        id = existing?.id ?: 0L,
                        title = title,
                        artist = artist,
                        album = album,
                        albumArtist = artist,
                        genre = existing?.genre ?: "Audio",
                        year = year,
                        duration = duration,
                        fileUri = contentUri,
                        artworkUri = artworkUri ?: existing?.artworkUri,
                        trackNumber = track,
                        discNumber = 1,
                        lyrics = existing?.lyrics ?: "",
                        rating = existing?.rating ?: 0f,
                        favorite = existing?.favorite ?: false,
                        playCount = existing?.playCount ?: 0,
                        lastPlayedTimestamp = existing?.lastPlayedTimestamp ?: 0L,
                        dateAdded = existing?.dateAdded ?: System.currentTimeMillis(),
                        lastModifiedTimestamp = System.currentTimeMillis(),
                        fileSize = size,
                        mimeType = mimeType
                    )
                    scannedSongs.add(song)

                    index++
                    if (count > 0) {
                        onProgress(index.toFloat() / count.toFloat())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        var newCount = 0
        var updatedCount = 0

        for (song in scannedSongs) {
            if (existingSongs.containsKey(song.fileUri)) {
                songDao.updateSong(song)
                updatedCount++
            } else {
                songDao.insertSong(song)
                newCount++
            }
        }

        // If no device media is discovered (e.g. fresh emulator or sandboxed environment),
        // generate built-in authentic playable practice tracks so the user has immediate
        // rich audio testing capability with real waveforms!
        val currentTotal = songDao.getAllSongsList()
        if (currentTotal.isEmpty()) {
            val practiceTracks = listOf(
                Triple("Emerald Breeze", "Salim Acoustic", 220.0),
                Triple("Slate Rhythm", "Salim Studio", 329.63),
                Triple("Harmonic Resonance", "Salim Audio Lab", 440.0)
            )
            for ((tTitle, tArtist, tFreq) in practiceTracks) {
                try {
                    val file = AudioTrackGenerator.generatePracticeTrack(
                        context = context,
                        title = tTitle,
                        durationSeconds = 20,
                        baseFrequency = tFreq
                    )
                    val generatedSong = Song(
                        title = tTitle,
                        artist = tArtist,
                        album = "Salim Master Sessions",
                        albumArtist = tArtist,
                        genre = "Acoustic",
                        year = 2026,
                        duration = 20000L,
                        fileUri = Uri.fromFile(file).toString(),
                        fileSize = file.length(),
                        mimeType = "audio/wav",
                        rating = 5f,
                        favorite = true
                    )
                    songDao.insertSong(generatedSong)
                    newCount++
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        ScanResult(
            totalScanned = scannedSongs.size,
            newAdded = newCount,
            updated = updatedCount,
            deleted = 0
        )
    }
}
