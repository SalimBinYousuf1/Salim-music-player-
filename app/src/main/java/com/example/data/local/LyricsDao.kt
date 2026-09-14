package com.example.data.local

import androidx.room.*
import com.example.domain.model.LyricsLine
import kotlinx.coroutines.flow.Flow

@Dao
interface LyricsDao {
    @Query("SELECT * FROM lyrics_lines WHERE songId = :songId ORDER BY timestamp ASC, position ASC")
    fun getLinesForSong(songId: Long): Flow<List<LyricsLine>>

    @Query("SELECT * FROM lyrics_lines WHERE songId = :songId ORDER BY timestamp ASC, position ASC")
    suspend fun getLinesForSongDirect(songId: Long): List<LyricsLine>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLine(line: LyricsLine): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<LyricsLine>)

    @Update
    suspend fun updateLine(line: LyricsLine)

    @Delete
    suspend fun deleteLine(line: LyricsLine)

    @Query("DELETE FROM lyrics_lines WHERE id = :id")
    suspend fun deleteLineById(id: Long)

    @Query("DELETE FROM lyrics_lines WHERE songId = :songId")
    suspend fun deleteLinesForSong(songId: Long)

    @Query("SELECT * FROM lyrics_lines")
    suspend fun getAllLyricsLines(): List<LyricsLine>
}
