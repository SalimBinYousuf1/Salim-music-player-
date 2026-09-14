package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.domain.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Song::class,
        Playlist::class,
        PlaylistSong::class,
        PlayHistory::class,
        LyricsLine::class,
        EqualizerPreset::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playHistoryDao(): PlayHistoryDao
    abstract fun lyricsDao(): LyricsDao
    abstract fun equalizerPresetDao(): EqualizerPresetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "salim_music_player.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDefaultPresets(database.equalizerPresetDao())
                    }
                }
            }

            suspend fun populateDefaultPresets(presetDao: EqualizerPresetDao) {
                val presets = listOf(
                    EqualizerPreset(
                        name = "Flat",
                        isCustom = false,
                        band60Hz = 0, band250Hz = 0, band1kHz = 0, band4kHz = 0, band12kHz = 0,
                        bassBoost = 0, virtualizer = 0, loudnessGain = 0
                    ),
                    EqualizerPreset(
                        name = "Bass Booster",
                        isCustom = false,
                        band60Hz = 800, band250Hz = 500, band1kHz = 0, band4kHz = -200, band12kHz = -400,
                        bassBoost = 700, virtualizer = 200, loudnessGain = 100
                    ),
                    EqualizerPreset(
                        name = "Vocal Focus",
                        isCustom = false,
                        band60Hz = -300, band250Hz = 200, band1kHz = 600, band4kHz = 500, band12kHz = 200,
                        bassBoost = 0, virtualizer = 100, loudnessGain = 0
                    ),
                    EqualizerPreset(
                        name = "Electronic Club",
                        isCustom = false,
                        band60Hz = 600, band250Hz = 400, band1kHz = -200, band4kHz = 300, band12kHz = 600,
                        bassBoost = 500, virtualizer = 400, loudnessGain = 150
                    ),
                    EqualizerPreset(
                        name = "Rock Punch",
                        isCustom = false,
                        band60Hz = 500, band250Hz = 300, band1kHz = -100, band4kHz = 400, band12kHz = 500,
                        bassBoost = 400, virtualizer = 200, loudnessGain = 100
                    ),
                    EqualizerPreset(
                        name = "Warm Jazz",
                        isCustom = false,
                        band60Hz = 300, band250Hz = 200, band1kHz = 100, band4kHz = 200, band12kHz = -100,
                        bassBoost = 200, virtualizer = 150, loudnessGain = 0
                    ),
                    EqualizerPreset(
                        name = "Acoustic Air",
                        isCustom = false,
                        band60Hz = 200, band250Hz = 100, band1kHz = 200, band4kHz = 400, band12kHz = 600,
                        bassBoost = 100, virtualizer = 300, loudnessGain = 50
                    )
                )
                presetDao.insertPresets(presets)
            }
        }
    }
}
