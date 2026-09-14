package com.example

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.domain.model.Song
import com.example.ui.navigation.SalimNavHost
import com.example.ui.theme.SalimMusicPlayerTheme
import com.example.ui.viewmodel.MusicPlayerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MusicPlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            val appTheme by viewModel.appTheme.collectAsState()
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamicColors by viewModel.dynamicColors.collectAsState()
            val highContrast by viewModel.highContrast.collectAsState()

            // Request necessary permissions gracefully
            val permissions = remember {
                val list = mutableListOf<String>()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    list.add(Manifest.permission.READ_MEDIA_AUDIO)
                    list.add(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    list.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                list.add(Manifest.permission.RECORD_AUDIO)
                list.toTypedArray()
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { results ->
                val audioGranted = results[Manifest.permission.READ_MEDIA_AUDIO] == true ||
                        results[Manifest.permission.READ_EXTERNAL_STORAGE] == true
                if (audioGranted) {
                    viewModel.scanDeviceMedia()
                }
            }

            LaunchedEffect(Unit) {
                permissionLauncher.launch(permissions)
            }

            SalimMusicPlayerTheme(
                theme = appTheme,
                themeMode = themeMode,
                dynamicColor = dynamicColors,
                highContrast = highContrast
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SalimNavHost(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val uri: Uri? = intent.data
            if (uri != null) {
                val fileName = uri.lastPathSegment ?: "External Audio"
                val song = Song(
                    title = fileName,
                    artist = "External Source",
                    album = "Direct Playback",
                    duration = 0L,
                    fileUri = uri.toString()
                )
                viewModel.playSong(song)
            }
        }
    }
}
