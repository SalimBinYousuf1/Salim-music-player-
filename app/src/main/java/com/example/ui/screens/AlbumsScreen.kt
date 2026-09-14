package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.Song
import com.example.ui.components.*
import com.example.ui.viewmodel.MusicPlayerViewModel

data class AlbumItem(
    val title: String,
    val artist: String,
    val songs: List<Song>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(
    viewModel: MusicPlayerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToNowPlaying: () -> Unit,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.rawSongs.collectAsState()
    val playerUiState by viewModel.playerUiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedAlbum by remember { mutableStateOf<AlbumItem?>(null) }

    // Group songs into albums
    val albums = remember(songs, searchQuery) {
        songs.groupBy { it.album.ifBlank { "Unknown Album" } }
            .map { (albumTitle, songList) ->
                AlbumItem(
                    title = albumTitle,
                    artist = songList.firstOrNull()?.artist?.ifBlank { "Unknown Artist" } ?: "Unknown Artist",
                    songs = songList
                )
            }
            .filter {
                if (searchQuery.isBlank()) true
                else it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true)
            }
            .sortedBy { it.title.lowercase() }
    }

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
                            text = if (selectedAlbum != null) selectedAlbum!!.title else "Albums (${albums.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (selectedAlbum != null) {
                                    selectedAlbum = null
                                } else {
                                    onNavigateBack()
                                }
                            },
                            modifier = Modifier.testTag("albums_back_button")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                if (selectedAlbum != null) {
                    val currentAlbum = selectedAlbum!!

                    // Album Hero Header Card
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Album,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentAlbum.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentAlbum.artist,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${currentAlbum.songs.size} tracks",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Action Buttons: Play All & Shuffle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassButton(
                            onClick = {
                                if (currentAlbum.songs.isNotEmpty()) {
                                    viewModel.playQueue(currentAlbum.songs, 0)
                                    onNavigateToNowPlaying()
                                }
                            },
                            text = "Play All",
                            leadingIcon = Icons.Default.PlayArrow,
                            modifier = Modifier.weight(1f).testTag("album_play_all")
                        )
                        GlassButton(
                            onClick = {
                                if (currentAlbum.songs.isNotEmpty()) {
                                    viewModel.playQueue(currentAlbum.songs.shuffled(), 0)
                                    onNavigateToNowPlaying()
                                }
                            },
                            text = "Shuffle",
                            leadingIcon = Icons.Default.Shuffle,
                            modifier = Modifier.weight(1f).testTag("album_shuffle")
                        )
                    }

                    // Song List for Album
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(items = currentAlbum.songs, key = { it.id }) { song ->
                            SongListItem(
                                song = song,
                                isPlaying = playerUiState.isPlaying,
                                isCurrentSong = playerUiState.currentSong?.id == song.id,
                                isSelected = false,
                                isBatchMode = false,
                                onSongClick = {
                                    viewModel.playQueue(currentAlbum.songs, currentAlbum.songs.indexOf(song))
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
                } else {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("albums_search_bar"),
                        placeholder = { Text("Search albums...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    // Grid of Album Cards
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(items = albums, key = { it.title }) { album ->
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("album_card_${album.title}"),
                                onClick = { selectedAlbum = album }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Album,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(54.dp)
                                            )
                                            // Mini vinyl badge at top right
                                            Surface(
                                                shape = CircleShape,
                                                color = Color.Black.copy(alpha = 0.6f),
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                                    .size(26.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        Icons.Default.MusicNote,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = album.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = album.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = "${album.songs.size} tracks",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
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
