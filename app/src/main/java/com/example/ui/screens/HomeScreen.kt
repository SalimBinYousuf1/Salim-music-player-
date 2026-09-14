package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.Playlist
import com.example.domain.model.Song
import com.example.ui.components.*
import com.example.ui.viewmodel.MusicPlayerViewModel

@Composable
fun HomeScreen(
    viewModel: MusicPlayerViewModel,
    onNavigateToSongs: () -> Unit,
    onNavigateToPlaylists: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToNowPlaying: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onOpenSongMenu: (Song) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToVisualizer: (() -> Unit)? = null,
    onNavigateToAlbums: (() -> Unit)? = null,
    onNavigateToArtists: (() -> Unit)? = null,
    onNavigateToGenres: (() -> Unit)? = null,
    onNavigateToDrivingMode: (() -> Unit)? = null,
    onNavigateToRecentlyPlayed: (() -> Unit)? = null,
    onNavigateToMostPlayed: (() -> Unit)? = null
) {
    val songs by viewModel.rawSongs.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val mostPlayed by viewModel.mostPlayed.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val playerUiState by viewModel.playerUiState.collectAsState()

    var showStatsDialog by remember { mutableStateOf(false) }

    FluidGlassBackground(
        isPlaying = playerUiState.isPlaying,
        primaryColor = MaterialTheme.colorScheme.primary,
        secondaryColor = MaterialTheme.colorScheme.secondary
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .testTag("home_screen"),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Quick Navigation Pills Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassPill(
                        text = "Songs (${songs.size})",
                        selected = false,
                        icon = Icons.Default.MusicNote,
                        onClick = onNavigateToSongs
                    )
                    if (onNavigateToAlbums != null) {
                        GlassPill(
                            text = "Albums",
                            selected = false,
                            icon = Icons.Default.Album,
                            onClick = onNavigateToAlbums
                        )
                    }
                    if (onNavigateToArtists != null) {
                        GlassPill(
                            text = "Artists",
                            selected = false,
                            icon = Icons.Default.Person,
                            onClick = onNavigateToArtists
                        )
                    }
                    if (onNavigateToGenres != null) {
                        GlassPill(
                            text = "Genres",
                            selected = false,
                            icon = Icons.Default.Category,
                            onClick = onNavigateToGenres
                        )
                    }
                    GlassPill(
                        text = "Playlists",
                        selected = false,
                        icon = Icons.Default.PlaylistPlay,
                        onClick = onNavigateToPlaylists
                    )
                    GlassPill(
                        text = "Favorites",
                        selected = false,
                        icon = Icons.Default.Favorite,
                        onClick = onNavigateToFavorites
                    )
                    if (onNavigateToDrivingMode != null) {
                        GlassPill(
                            text = "Drive Mode",
                            selected = false,
                            icon = Icons.Default.DirectionsCar,
                            onClick = onNavigateToDrivingMode
                        )
                    }
                    GlassPill(
                        text = "Insights",
                        selected = false,
                        icon = Icons.Default.Insights,
                        onClick = { showStatsDialog = true }
                    )
                    GlassPill(
                        text = "Equalizer",
                        selected = false,
                        icon = Icons.Default.Tune,
                        onClick = onNavigateToEqualizer
                    )
                    if (onNavigateToVisualizer != null) {
                        GlassPill(
                            text = "Beat Visualizer",
                            selected = true,
                            icon = Icons.Default.GraphicEq,
                            onClick = onNavigateToVisualizer
                        )
                    }
                }
            }

        // Quick Resume Hero Header
        item {
            val currentSong = playerUiState.currentSong ?: recentlyPlayed.firstOrNull()
            if (currentSong != null) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("hero_resume_card"),
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    onClick = {
                        if (playerUiState.currentSong == null) {
                            viewModel.playSong(currentSong)
                        }
                        onNavigateToNowPlaying()
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (playerUiState.isPlaying && playerUiState.currentSong?.id == currentSong.id) {
                                    GlassBeatIndicator(
                                        isPlaying = true,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (playerUiState.isPlaying) "NOW PLAYING" else "JUMP BACK IN",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = currentSong.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentSong.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        GlassIconButton(
                            onClick = {
                                if (playerUiState.currentSong?.id == currentSong.id) {
                                    viewModel.togglePlayPause()
                                } else {
                                    viewModel.playSong(currentSong)
                                }
                            },
                            icon = if (playerUiState.isPlaying && playerUiState.currentSong?.id == currentSong.id) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            size = 48.dp,
                            iconSize = 26.dp,
                            tint = Color.Black,
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            borderColor = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.testTag("hero_play_button")
                        )
                    }
                }
            }
        }

        // Quick Action Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassButton(
                    onClick = {
                        if (songs.isNotEmpty()) {
                            viewModel.playQueue(songs, 0)
                        }
                    },
                    leadingIcon = Icons.Default.PlayArrow,
                    text = "Play All",
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_play_all_button")
                )

                GlassButton(
                    onClick = {
                        if (songs.isNotEmpty()) {
                            viewModel.toggleShuffle()
                            val randIdx = songs.indices.random()
                            viewModel.playQueue(songs, randIdx)
                        }
                    },
                    leadingIcon = Icons.Default.Shuffle,
                    text = "Shuffle",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    borderColor = Color.White.copy(alpha = 0.22f),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_shuffle_all_button")
                )

                GlassIconButton(
                    onClick = { viewModel.scanDeviceMedia() },
                    icon = Icons.Default.Sync,
                    contentDescription = "Scan Media",
                    size = 48.dp,
                    iconSize = 22.dp,
                    modifier = Modifier.testTag("home_scan_button")
                )
            }
        }

        // Playlists Horizontal Row
        if (playlists.isNotEmpty()) {
            item {
                SectionHeader(title = "Playlists", onActionClick = onNavigateToPlaylists)
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    playlists.forEach { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            onClick = onNavigateToPlaylists
                        )
                    }
                }
            }
        }

        // Favorites Row
        if (favorites.isNotEmpty()) {
            item {
                SectionHeader(title = "Favorites (${favorites.size})", onActionClick = onNavigateToFavorites)
            }
            items(favorites.take(4), key = { "fav_${it.id}" }) { song ->
                SongListItem(
                    song = song,
                    isPlaying = playerUiState.isPlaying && playerUiState.currentSong?.id == song.id,
                    isCurrentSong = playerUiState.currentSong?.id == song.id,
                    isSelected = false,
                    isBatchMode = false,
                    onSongClick = { viewModel.playSong(song) },
                    onSongLongClick = { onOpenSongMenu(song) },
                    onToggleSelect = {},
                    onToggleFavorite = { viewModel.toggleFavorite(song) },
                    onPlayNext = { viewModel.playbackManager.queueManager.playNext(song) },
                    onAddToPlaylist = { onOpenSongMenu(song) },
                    onEditSong = { onOpenSongMenu(song) },
                    onDeleteSong = { viewModel.deleteSong(song, false) },
                    onViewLyrics = onNavigateToNowPlaying
                )
            }
        }

        // Recently Played
        if (recentlyPlayed.isNotEmpty()) {
            item {
                SectionHeader(title = "Recently Played", onActionClick = onNavigateToSongs)
            }
            items(recentlyPlayed.take(4), key = { "rec_${it.id}" }) { song ->
                SongListItem(
                    song = song,
                    isPlaying = playerUiState.isPlaying && playerUiState.currentSong?.id == song.id,
                    isCurrentSong = playerUiState.currentSong?.id == song.id,
                    isSelected = false,
                    isBatchMode = false,
                    onSongClick = { viewModel.playSong(song) },
                    onSongLongClick = { onOpenSongMenu(song) },
                    onToggleSelect = {},
                    onToggleFavorite = { viewModel.toggleFavorite(song) },
                    onPlayNext = { viewModel.playbackManager.queueManager.playNext(song) },
                    onAddToPlaylist = { onOpenSongMenu(song) },
                    onEditSong = { onOpenSongMenu(song) },
                    onDeleteSong = { viewModel.deleteSong(song, false) },
                    onViewLyrics = onNavigateToNowPlaying
                )
            }
        }

        // Most Played
        if (mostPlayed.isNotEmpty()) {
            item {
                SectionHeader(title = "Most Played", onActionClick = onNavigateToSongs)
            }
            items(mostPlayed.take(4), key = { "most_${it.id}" }) { song ->
                SongListItem(
                    song = song,
                    isPlaying = playerUiState.isPlaying && playerUiState.currentSong?.id == song.id,
                    isCurrentSong = playerUiState.currentSong?.id == song.id,
                    isSelected = false,
                    isBatchMode = false,
                    onSongClick = { viewModel.playSong(song) },
                    onSongLongClick = { onOpenSongMenu(song) },
                    onToggleSelect = {},
                    onToggleFavorite = { viewModel.toggleFavorite(song) },
                    onPlayNext = { viewModel.playbackManager.queueManager.playNext(song) },
                    onAddToPlaylist = { onOpenSongMenu(song) },
                    onEditSong = { onOpenSongMenu(song) },
                    onDeleteSong = { viewModel.deleteSong(song, false) },
                    onViewLyrics = onNavigateToNowPlaying
                )
            }
        }
    }

    if (showStatsDialog) {
        MusicStatsDialog(
            songs = songs,
            onDismiss = { showStatsDialog = false }
        )
    }
}
}

@Composable
fun SectionHeader(
    title: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        TextButton(
            onClick = onActionClick,
            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
        ) {
            Text("See All")
        }
    }
}

@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .size(116.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlaylistPlay,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
