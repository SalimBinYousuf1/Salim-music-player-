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

data class ArtistItem(
    val name: String,
    val songs: List<Song>,
    val albumCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistsScreen(
    viewModel: MusicPlayerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToNowPlaying: () -> Unit,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.rawSongs.collectAsState()
    val playerUiState by viewModel.playerUiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedArtist by remember { mutableStateOf<ArtistItem?>(null) }

    // Group songs by artist
    val artists = remember(songs, searchQuery) {
        songs.groupBy { it.artist.ifBlank { "Unknown Artist" } }
            .map { (artistName, songList) ->
                val albumsCount = songList.map { it.album }.distinct().size
                ArtistItem(
                    name = artistName,
                    songs = songList,
                    albumCount = albumsCount
                )
            }
            .filter {
                if (searchQuery.isBlank()) true
                else it.name.contains(searchQuery, ignoreCase = true)
            }
            .sortedBy { it.name.lowercase() }
    }

    FluidGlassBackground(
        isPlaying = playerUiState.isPlaying,
        primaryColor = MaterialTheme.colorScheme.tertiary,
        secondaryColor = MaterialTheme.colorScheme.primary
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (selectedArtist != null) selectedArtist!!.name else "Artists (${artists.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (selectedArtist != null) {
                                    selectedArtist = null
                                } else {
                                    onNavigateBack()
                                }
                            },
                            modifier = Modifier.testTag("artists_back_button")
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
                if (selectedArtist != null) {
                    val currentArtist = selectedArtist!!

                    // Artist Hero Banner Card
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
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentArtist.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${currentArtist.songs.size} tracks • ${currentArtist.albumCount} albums",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Play All & Shuffle Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassButton(
                            onClick = {
                                if (currentArtist.songs.isNotEmpty()) {
                                    viewModel.playQueue(currentArtist.songs, 0)
                                    onNavigateToNowPlaying()
                                }
                            },
                            text = "Play All",
                            leadingIcon = Icons.Default.PlayArrow,
                            modifier = Modifier.weight(1f).testTag("artist_play_all")
                        )
                        GlassButton(
                            onClick = {
                                if (currentArtist.songs.isNotEmpty()) {
                                    viewModel.playQueue(currentArtist.songs.shuffled(), 0)
                                    onNavigateToNowPlaying()
                                }
                            },
                            text = "Shuffle",
                            leadingIcon = Icons.Default.Shuffle,
                            modifier = Modifier.weight(1f).testTag("artist_shuffle")
                        )
                    }

                    // Songs by Artist
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(items = currentArtist.songs, key = { it.id }) { song ->
                            SongListItem(
                                song = song,
                                isPlaying = playerUiState.isPlaying,
                                isCurrentSong = playerUiState.currentSong?.id == song.id,
                                isSelected = false,
                                isBatchMode = false,
                                onSongClick = {
                                    viewModel.playQueue(currentArtist.songs, currentArtist.songs.indexOf(song))
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
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("artists_search_bar"),
                        placeholder = { Text("Search artists...") },
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

                    // Grid of Artist Cards
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 140.dp),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(items = artists, key = { it.name }) { artist ->
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("artist_card_${artist.name}"),
                                onClick = { selectedArtist = artist }
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        modifier = Modifier.size(72.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(38.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = artist.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "${artist.songs.size} tracks",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
