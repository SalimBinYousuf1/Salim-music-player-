package com.example

import android.app.Application
import com.example.data.backup.BackupRestoreManager
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.repository.MusicRepository
import com.example.player.controller.PlaybackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SalimApplication : Application() {

    val applicationScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val preferencesManager by lazy { PreferencesManager(this) }
    val repository by lazy { MusicRepository(this, database, preferencesManager) }
    val backupRestoreManager by lazy { BackupRestoreManager(this, database, preferencesManager) }

    val playbackManager by lazy {
        PlaybackManager(
            context = this,
            onTrackCompleted = { song ->
                applicationScope.launch {
                    repository.incrementPlayCount(song.id)
                }
            },
            onPlaybackPositionSaved = { songId, position ->
                applicationScope.launch {
                    preferencesManager.setLastPlayback(songId, position)
                }
            }
        )
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize MediaStore initial scan in background
        applicationScope.launch {
            try {
                repository.scanMedia()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
