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
import com.example.domain.model.Song
import com.example.domain.model.SortOption
import com.example.ui.components.*
import com.example.ui.viewmodel.MusicPlayerViewModel

@Composable
fun SongsScreen(
    viewModel: MusicPlayerViewModel,
    onNavigateToNowPlaying: () -> Unit,
    onOpenSongMenu: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.sortedAndFilteredSongs.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val selectedGenre by viewModel.selectedGenreFilter.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val playerUiState by viewModel.playerUiState.collectAsState()
    val selectedSongIds by viewModel.selectedSongIds.collectAsState()
    val isBatchMode by viewModel.isBatchMode.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var songToEdit by remember { mutableStateOf<Song?>(null) }
    var songToDelete by remember { mutableStateOf<Song?>(null) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().testTag("songs_screen")) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Bar with Sort & Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${songs.size} Tracks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Sort Button
                    Box {
                        FilledTonalButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("sort_button")
                        ) {
                            Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(sortOption.displayName)
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.displayName) },
                                    onClick = {
                                        viewModel.setSortOption(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.size(48.dp).testTag("add_practice_track_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Generate Audio Track")
                    }
                }
            }

            // Genre Horizontal Filter Chips
            val allGenres = listOf("All") + genres
            ResponsiveChipGroup(
                items = allGenres,
                selectedItem = selectedGenre ?: "All",
                onItemSelected = { selected ->
                    viewModel.setFilterGenre(if (selected == "All") null else selected)
                },
                labelProvider = { it }
            )

            // Batch Action Bar
            if (isBatchMode) {
                BatchActionBar(
                    selectedCount = selectedSongIds.size,
                    onClearSelection = { viewModel.clearSelection() },
                    onBatchFavorite = { viewModel.batchFavorite(true) },
                    onBatchAddToPlaylist = { /* show playlist picker */ },
                    onBatchSetGenre = { /* show genre picker */ },
                    onBatchDelete = { showBatchDeleteDialog = true }
                )
            }

            // Songs List
            if (songs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.MusicOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No songs found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Generate Practice Audio Track")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(songs, key = { it.id }) { song ->
                        val isCurrent = playerUiState.currentSong?.id == song.id
                        val isPlaying = isCurrent && playerUiState.isPlaying
                        val isSelected = selectedSongIds.contains(song.id)

                        SongListItem(
                            song = song,
                            isPlaying = isPlaying,
                            isCurrentSong = isCurrent,
                            isSelected = isSelected,
                            isBatchMode = isBatchMode,
                            onSongClick = {
                                if (isCurrent) {
                                    onNavigateToNowPlaying()
                                } else {
                                    val index = songs.indexOf(song)
                                    viewModel.playQueue(songs, index)
                                }
                            },
                            onSongLongClick = {
                                viewModel.toggleSelectSong(song.id)
                            },
                            onToggleSelect = {
                                viewModel.toggleSelectSong(song.id)
                            },
                            onToggleFavorite = {
                                viewModel.toggleFavorite(song)
                            },
                            onPlayNext = {
                                viewModel.playbackManager.queueManager.playNext(song)
                            },
                            onAddToPlaylist = {
                                onOpenSongMenu(song)
                            },
                            onEditSong = {
                                songToEdit = song
                            },
                            onDeleteSong = {
                                songToDelete = song
                            },
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

    // Add / Generate Practice Track Dialog
    if (showAddDialog) {
        AddSongDialog(
            onDismiss = { showAddDialog = false },
            onGeneratePracticeTrack = { title, durationSec ->
                viewModel.generatePracticeTrack(title, durationSec)
            }
        )
    }

    // Edit Song Dialog
    songToEdit?.let { song ->
        SongEditDialog(
            song = song,
            onDismiss = { songToEdit = null },
            onSave = { updated ->
                viewModel.updateSong(updated)
                songToEdit = null
            }
        )
    }

    // Confirm Single Delete Dialog
    songToDelete?.let { song ->
        ConfirmDeleteDialog(
            title = "Delete Track",
            message = "Are you sure you want to remove '${song.title}' from your library?",
            onDismiss = { songToDelete = null },
            onConfirm = { deleteFile ->
                viewModel.deleteSong(song, deleteFile)
                songToDelete = null
            }
        )
    }

    // Confirm Batch Delete Dialog
    if (showBatchDeleteDialog) {
        ConfirmDeleteDialog(
            title = "Delete Selected Tracks",
            message = "Are you sure you want to delete ${selectedSongIds.size} selected tracks?",
            onDismiss = { showBatchDeleteDialog = false },
            onConfirm = { deleteFiles ->
                viewModel.batchDelete(deleteFiles)
                showBatchDeleteDialog = false
            }
        )
    }
}
