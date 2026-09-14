package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.*
import com.example.ui.viewmodel.MusicPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyPlayedScreen(
    viewModel: MusicPlayerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToNowPlaying: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recentSongs by viewModel.recentlyPlayed.collectAsState()
    val playerUiState by viewModel.playerUiState.collectAsState()

    FluidGlassBackground(
        isPlaying = playerUiState.isPlaying,
        primaryColor = MaterialTheme.colorScheme.primary,
        secondaryColor = MaterialTheme.colorScheme.secondary
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Recently Played (${recentSongs.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("recent_back_button")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                if (recentSongs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(96.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Recent Activity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Songs you listen to will automatically appear here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Play All & Shuffle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassButton(
                            onClick = {
                                if (recentSongs.isNotEmpty()) {
                                    viewModel.playQueue(recentSongs, 0)
                                    onNavigateToNowPlaying()
                                }
                            },
                            text = "Play All",
                            leadingIcon = Icons.Default.PlayArrow,
                            modifier = Modifier.weight(1f).testTag("recent_play_all")
                        )
                        GlassButton(
                            onClick = {
                                if (recentSongs.isNotEmpty()) {
                                    viewModel.playQueue(recentSongs.shuffled(), 0)
                                    onNavigateToNowPlaying()
                                }
                            },
                            text = "Shuffle",
                            leadingIcon = Icons.Default.Shuffle,
                            modifier = Modifier.weight(1f).testTag("recent_shuffle")
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(items = recentSongs, key = { it.id }) { song ->
                            SongListItem(
                                song = song,
                                isPlaying = playerUiState.isPlaying,
                                isCurrentSong = playerUiState.currentSong?.id == song.id,
                                isSelected = false,
                                isBatchMode = false,
                                onSongClick = {
                                    viewModel.playQueue(recentSongs, recentSongs.indexOf(song))
                                    onNavigateToNowPlaying()
                                },
                                onSongLongClick = {},
                                onToggleSelect = {},
                                onToggleFavorite = { viewModel.toggleFavorite(song) },
                                onPlayNext = { viewModel.playbackManager.queueManager.playNext(song) },
                                onAddToPlaylist = {},
                                onEditSong = {},
                                onDeleteSong = { viewModel.deleteSong(song, false) },
                                onViewLyrics = {}
                            )
                        }
                    }
                }
            }
        }
    }
}
