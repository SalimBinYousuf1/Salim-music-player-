package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
fun MostPlayedScreen(
    viewModel: MusicPlayerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToNowPlaying: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topSongs by viewModel.mostPlayed.collectAsState()
    val playerUiState by viewModel.playerUiState.collectAsState()

    FluidGlassBackground(
        isPlaying = playerUiState.isPlaying,
        primaryColor = Color(0xFFF59E0B),
        secondaryColor = MaterialTheme.colorScheme.primary
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Top Most Played",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("most_played_back_button")
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
                if (topSongs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                modifier = Modifier.size(96.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Play Stats Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Your most played heavy rotation tracks will rank here automatically.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Action Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassButton(
                            onClick = {
                                if (topSongs.isNotEmpty()) {
                                    viewModel.playQueue(topSongs, 0)
                                    onNavigateToNowPlaying()
                                }
                            },
                            text = "Play Top Hits",
                            leadingIcon = Icons.Default.PlayArrow,
                            modifier = Modifier.weight(1f).testTag("most_played_play_all")
                        )
                        GlassButton(
                            onClick = {
                                if (topSongs.isNotEmpty()) {
                                    viewModel.playQueue(topSongs.shuffled(), 0)
                                    onNavigateToNowPlaying()
                                }
                            },
                            text = "Shuffle",
                            leadingIcon = Icons.Default.Shuffle,
                            modifier = Modifier.weight(1f).testTag("most_played_shuffle")
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        itemsIndexed(items = topSongs, key = { _, song -> song.id }) { index, song ->
                            val rankColor = when (index) {
                                0 -> Color(0xFFFFD700) // Gold
                                1 -> Color(0xFFC0C0C0) // Silver
                                2 -> Color(0xFFCD7F32) // Bronze
                                else -> Color.White.copy(alpha = 0.7f)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = rankColor.copy(alpha = 0.2f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = rankColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(modifier = Modifier.weight(1f)) {
                                    SongListItem(
                                        song = song,
                                        isPlaying = playerUiState.isPlaying,
                                        isCurrentSong = playerUiState.currentSong?.id == song.id,
                                        isSelected = false,
                                        isBatchMode = false,
                                        onSongClick = {
                                            viewModel.playQueue(topSongs, index)
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
    }
}
