package com.example.data.local

import androidx.room.*
import com.example.domain.model.PlayHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayHistoryDao {
    @Query("SELECT * FROM play_history ORDER BY startedTimestamp DESC LIMIT :limit")
    fun getAllHistory(limit: Int = 200): Flow<List<PlayHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(playHistory: PlayHistory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistories(playHistories: List<PlayHistory>)

    @Query("DELETE FROM play_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM play_history WHERE songId = :songId")
    suspend fun deleteHistoryBySongId(songId: Long)

    @Query("DELETE FROM play_history")
    suspend fun clearAllHistory()

    @Query("SELECT * FROM play_history")
    suspend fun getAllHistoryList(): List<PlayHistory>
}
