package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.Song
import com.example.ui.components.*
import com.example.ui.viewmodel.MusicPlayerViewModel

data class GenreItem(
    val name: String,
    val songs: List<Song>,
    val gradientColors: List<Color>
)

private val GENRE_PALETTES = listOf(
    listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)),
    listOf(Color(0xFF3B82F6), Color(0xFF06B6D4)),
    listOf(Color(0xFF10B981), Color(0xFF059669)),
    listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),
    listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)),
    listOf(Color(0xFF14B8A6), Color(0xFF0D9488)),
    listOf(Color(0xFFF97316), Color(0xFFEA580C)),
    listOf(Color(0xFF64748B), Color(0xFF475569))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenresScreen(
    viewModel: MusicPlayerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToNowPlaying: () -> Unit,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.rawSongs.collectAsState()
    val playerUiState by viewModel.playerUiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf<GenreItem?>(null) }

    val genres = remember(songs, searchQuery) {
        val grouped = songs.groupBy { it.genre.ifBlank { "Unknown Genre" } }
        grouped.entries.toList().mapIndexed { index, entry ->
            GenreItem(
                name = entry.key,
                songs = entry.value,
                gradientColors = GENRE_PALETTES[index % GENRE_PALETTES.size]
            )
        }.filter {
            if (searchQuery.isBlank()) true
            else it.name.contains(searchQuery, ignoreCase = true)
        }.sortedByDescending { it.songs.size }
    }

    FluidGlassBackground(
        isPlaying = playerUiState.isPlaying,
        primaryColor = MaterialTheme.colorScheme.primary,
        secondaryColor = Color(0xFFEC4899)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (selectedGenre != null) selectedGenre!!.name else "Genres (${genres.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (selectedGenre != null) {
                                    selectedGenre = null
                                } else {
                                    onNavigateBack()
                                }
                            },
                            modifier = Modifier.testTag("genres_back_button")
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
                if (selectedGenre != null) {
                    val currentGenre = selectedGenre!!
                    // Hero Genre Banner
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = currentGenre.gradientColors.first().copy(alpha = 0.35f),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Category,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentGenre.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${currentGenre.songs.size} tracks in genre",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Play All & Shuffle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassButton(
                            onClick = {
                                if (currentGenre.songs.isNotEmpty()) {
                                    viewModel.playQueue(currentGenre.songs, 0)
                                    onNavigateToNowPlaying()
                                }
                            },
                            text = "Play Genre",
                            leadingIcon = Icons.Default.PlayArrow,
                            modifier = Modifier.weight(1f).testTag("genre_play_all")
                        )
                        GlassButton(
                            onClick = {
                                if (currentGenre.songs.isNotEmpty()) {
                                    viewModel.playQueue(currentGenre.songs.shuffled(), 0)
                                    onNavigateToNowPlaying()
                                }
                            },
                            text = "Shuffle",
                            leadingIcon = Icons.Default.Shuffle,
                            modifier = Modifier.weight(1f).testTag("genre_shuffle")
                        )
                    }

                    // Genre Song List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(items = currentGenre.songs, key = { it.id }) { song ->
                            SongListItem(
                                song = song,
                                isPlaying = playerUiState.isPlaying,
                                isCurrentSong = playerUiState.currentSong?.id == song.id,
                                isSelected = false,
                                isBatchMode = false,
                                onSongClick = {
                                    viewModel.playQueue(currentGenre.songs, currentGenre.songs.indexOf(song))
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
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("genres_search_bar"),
                        placeholder = { Text("Search genres...") },
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

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(items = genres, key = { it.name }) { genre ->
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .testTag("genre_card_${genre.name}"),
                                backgroundColor = genre.gradientColors.first().copy(alpha = 0.25f),
                                borderColor = genre.gradientColors.first().copy(alpha = 0.5f),
                                onClick = { selectedGenre = genre }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Category,
                                            contentDescription = null,
                                            tint = genre.gradientColors.first(),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "${genre.songs.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }

                                    Text(
                                        text = genre.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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
