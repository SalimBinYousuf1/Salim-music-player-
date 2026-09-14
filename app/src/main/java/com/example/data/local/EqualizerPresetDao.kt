package com.example.data.local

import androidx.room.*
import com.example.domain.model.EqualizerPreset
import kotlinx.coroutines.flow.Flow

@Dao
interface EqualizerPresetDao {
    @Query("SELECT * FROM equalizer_presets ORDER BY isCustom ASC, name ASC")
    fun getAllPresets(): Flow<List<EqualizerPreset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: EqualizerPreset): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresets(presets: List<EqualizerPreset>)

    @Update
    suspend fun updatePreset(preset: EqualizerPreset)

    @Delete
    suspend fun deletePreset(preset: EqualizerPreset)

    @Query("DELETE FROM equalizer_presets WHERE id = :id AND isCustom = 1")
    suspend fun deletePresetById(id: Long)

    @Query("SELECT * FROM equalizer_presets")
    suspend fun getAllPresetsList(): List<EqualizerPreset>
}
