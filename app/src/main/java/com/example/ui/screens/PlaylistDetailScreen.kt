package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.Playlist
import com.example.domain.model.Song
import com.example.ui.components.SongListItem
import com.example.ui.viewmodel.MusicPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    viewModel: MusicPlayerViewModel,
    onBack: () -> Unit,
    onNavigateToNowPlaying: () -> Unit,
    modifier: Modifier = Modifier
) {
    val songsFlow = remember(playlist.id) { viewModel.getSongsForPlaylist(playlist.id) }
    val songs by songsFlow.collectAsState(initial = emptyList())
    val playerUiState by viewModel.playerUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlist.name) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp).testTag("playlist_detail_back")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (songs.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.playQueue(songs, 0) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play All")
                        }
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize().testTag("playlist_detail_screen")
    ) { paddingValues ->
        if (songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No songs in this playlist yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(songs, key = { it.id }) { song ->
                    val isCurrent = playerUiState.currentSong?.id == song.id
                    val isPlaying = isCurrent && playerUiState.isPlaying

                    SongListItem(
                        song = song,
                        isPlaying = isPlaying,
                        isCurrentSong = isCurrent,
                        isSelected = false,
                        isBatchMode = false,
                        onSongClick = {
                            val index = songs.indexOf(song)
                            viewModel.playQueue(songs, index)
                            onNavigateToNowPlaying()
                        },
                        onSongLongClick = {},
                        onToggleSelect = {},
                        onToggleFavorite = { viewModel.toggleFavorite(song) },
                        onPlayNext = { viewModel.playbackManager.queueManager.playNext(song) },
                        onAddToPlaylist = {},
                        onEditSong = {},
                        onDeleteSong = {
                            viewModel.removeSongFromPlaylist(playlist.id, song.id)
                        },
                        onViewLyrics = onNavigateToNowPlaying
                    )
                }
            }
        }
    }
}
