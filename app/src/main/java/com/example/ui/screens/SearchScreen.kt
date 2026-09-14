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
import androidx.compose.ui.unit.dp
import com.example.domain.model.Song
import com.example.ui.components.SongListItem
import com.example.ui.viewmodel.MusicPlayerViewModel

@Composable
fun SearchScreen(
    viewModel: MusicPlayerViewModel,
    onNavigateToNowPlaying: () -> Unit,
    onOpenSongMenu: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val playerUiState by viewModel.playerUiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen")
    ) {
        // Search Bar
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search songs, artists, albums, lyrics...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.searchQuery.value = "" },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("search_input")
        )

        if (query.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Search your local music library",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No matching songs found for \"$query\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(searchResults, key = { it.id }) { song ->
                    val isCurrent = playerUiState.currentSong?.id == song.id
                    val isPlaying = isCurrent && playerUiState.isPlaying

                    SongListItem(
                        song = song,
                        isPlaying = isPlaying,
                        isCurrentSong = isCurrent,
                        isSelected = false,
                        isBatchMode = false,
                        onSongClick = {
                            viewModel.playSong(song)
                            onNavigateToNowPlaying()
                        },
                        onSongLongClick = { onOpenSongMenu(song) },
                        onToggleSelect = {},
                        onToggleFavorite = { viewModel.toggleFavorite(song) },
                        onPlayNext = { viewModel.playbackManager.queueManager.playNext(song) },
                        onAddToPlaylist = { onOpenSongMenu(song) },
                        onEditSong = { onOpenSongMenu(song) },
                        onDeleteSong = { viewModel.deleteSong(song, false) },
                        onViewLyrics = {
                            viewModel.playSong(song)
                            onNavigateToNowPlaying()
                        }
                    )
                }
            }
        }
    }
}
