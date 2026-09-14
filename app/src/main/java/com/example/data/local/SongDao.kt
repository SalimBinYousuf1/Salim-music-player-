package com.example.data.local

import androidx.room.*
import com.example.domain.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE ASC")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    fun getSongById(id: Long): Flow<Song?>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongByIdDirect(id: Long): Song?

    @Query("SELECT * FROM songs WHERE fileUri = :fileUri LIMIT 1")
    suspend fun getSongByUri(fileUri: String): Song?

    @Query("SELECT * FROM songs WHERE id IN (:ids)")
    suspend fun getSongsByIdsDirect(ids: List<Long>): List<Song>

    @Query("SELECT * FROM songs WHERE id IN (:ids)")
    fun getSongsByIds(ids: List<Long>): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE favorite = 1 ORDER BY title COLLATE NOCASE ASC")
    fun getFavorites(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE lastPlayedTimestamp > 0 ORDER BY lastPlayedTimestamp DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 100): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE playCount > 0 ORDER BY playCount DESC LIMIT :limit")
    fun getMostPlayed(limit: Int = 100): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE album = :album ORDER BY trackNumber ASC, title COLLATE NOCASE ASC")
    fun getSongsByAlbum(album: String): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE artist = :artist ORDER BY year DESC, title COLLATE NOCASE ASC")
    fun getSongsByArtist(artist: String): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE genre = :genre ORDER BY title COLLATE NOCASE ASC")
    fun getSongsByGenre(genre: String): Flow<List<Song>>

    @Query("""
        SELECT * FROM songs 
        WHERE title LIKE '%' || :query || '%' 
           OR artist LIKE '%' || :query || '%' 
           OR album LIKE '%' || :query || '%' 
           OR albumArtist LIKE '%' || :query || '%' 
           OR genre LIKE '%' || :query || '%' 
           OR lyrics LIKE '%' || :query || '%' 
           OR fileUri LIKE '%' || :query || '%'
        ORDER BY title COLLATE NOCASE ASC
    """)
    fun searchSongs(query: String): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>): List<Long>

    @Update
    suspend fun updateSong(song: Song)

    @Query("UPDATE songs SET favorite = :favorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, favorite: Boolean)

    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE id = :id")
    suspend fun incrementPlayCount(id: Long, timestamp: Long)

    @Query("UPDATE songs SET rating = :rating WHERE id = :id")
    suspend fun updateRating(id: Long, rating: Float)

    @Query("UPDATE songs SET genre = :genre WHERE id IN (:ids)")
    suspend fun batchUpdateGenre(ids: List<Long>, genre: String)

    @Query("UPDATE songs SET rating = :rating WHERE id IN (:ids)")
    suspend fun batchUpdateRating(ids: List<Long>, rating: Float)

    @Query("UPDATE songs SET favorite = :favorite WHERE id IN (:ids)")
    suspend fun batchUpdateFavorite(ids: List<Long>, favorite: Boolean)

    @Query("UPDATE songs SET playCount = :playCount WHERE id IN (:ids)")
    suspend fun batchUpdatePlayCount(ids: List<Long>, playCount: Int)

    @Delete
    suspend fun deleteSong(song: Song)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSongById(id: Long)

    @Query("DELETE FROM songs WHERE id IN (:ids)")
    suspend fun deleteSongsByIds(ids: List<Long>)

    @Query("SELECT DISTINCT genre FROM songs WHERE genre != '' AND genre != 'All' ORDER BY genre COLLATE NOCASE ASC")
    fun getDistinctGenres(): Flow<List<String>>

    @Query("SELECT DISTINCT album FROM songs WHERE album != '' ORDER BY album COLLATE NOCASE ASC")
    fun getDistinctAlbums(): Flow<List<String>>

    @Query("SELECT DISTINCT artist FROM songs WHERE artist != '' ORDER BY artist COLLATE NOCASE ASC")
    fun getDistinctArtists(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM songs")
    fun getSongsCount(): Flow<Int>

    @Query("SELECT * FROM songs")
    suspend fun getAllSongsList(): List<Song>
}
