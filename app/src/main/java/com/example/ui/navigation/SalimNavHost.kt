package com.example.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.domain.model.Playlist
import com.example.domain.model.Song
import com.example.ui.components.MiniPlayer
import com.example.ui.screens.*
import com.example.ui.viewmodel.MusicPlayerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalimNavHost(
    viewModel: MusicPlayerViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val playerUiState by viewModel.playerUiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedPlaylistForDetail by remember { mutableStateOf<Playlist?>(null) }
    var playlistPickerForSong by remember { mutableStateOf<Song?>(null) }

    val isNowPlayingScreen = currentRoute == Screen.NowPlaying.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Salim Music",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                DrawerScreens.forEach { screen ->
                    NavigationDrawerItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("drawer_item_${screen.route}")
                    )
                }
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                if (!isNowPlayingScreen) {
                    Column {
                        // Mini Player
                        if (playerUiState.currentSong != null) {
                            MiniPlayer(
                                uiState = playerUiState,
                                onMiniPlayerClick = {
                                    navController.navigate(Screen.NowPlaying.route)
                                },
                                onTogglePlayPause = { viewModel.togglePlayPause() },
                                onPlayNext = { viewModel.playNext() },
                                onToggleFavorite = {
                                    playerUiState.currentSong?.let { viewModel.toggleFavorite(it) }
                                }
                            )
                        }

                        // Bottom Navigation Bar
                        NavigationBar(modifier = Modifier.testTag("bottom_nav_bar")) {
                            BottomNavScreens.forEach { screen ->
                                val isSelected = currentRoute == screen.route
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title) },
                                    selected = isSelected,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(Screen.Home.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    modifier = Modifier.testTag("bottom_nav_${screen.route}")
                                )
                            }
                        }
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Home
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToSongs = { navController.navigate(Screen.Songs.route) },
                        onNavigateToPlaylists = { navController.navigate(Screen.Playlists.route) },
                        onNavigateToFavorites = {
                            viewModel.setSortOption(com.example.domain.model.SortOption.FAVORITES_FIRST)
                            navController.navigate(Screen.Songs.route)
                        },
                        onNavigateToNowPlaying = { navController.navigate(Screen.NowPlaying.route) },
                        onNavigateToEqualizer = { navController.navigate(Screen.Equalizer.route) },
                        onOpenSongMenu = { song -> playlistPickerForSong = song },
                        onNavigateToVisualizer = { navController.navigate(Screen.Visualizer.route) }
                    )
                }

                // Songs
                composable(Screen.Songs.route) {
                    SongsScreen(
                        viewModel = viewModel,
                        onNavigateToNowPlaying = { navController.navigate(Screen.NowPlaying.route) },
                        onOpenSongMenu = { song -> playlistPickerForSong = song }
                    )
                }

                // Playlists
                composable(Screen.Playlists.route) {
                    PlaylistsScreen(
                        viewModel = viewModel,
                        onPlaylistClick = { playlist ->
                            selectedPlaylistForDetail = playlist
                            navController.navigate("playlist_detail")
                        }
                    )
                }

                // Playlist Detail
                composable("playlist_detail") {
                    selectedPlaylistForDetail?.let { playlist ->
                        PlaylistDetailScreen(
                            playlist = playlist,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToNowPlaying = { navController.navigate(Screen.NowPlaying.route) }
                        )
                    }
                }

                // Search
                composable(Screen.Search.route) {
                    SearchScreen(
                        viewModel = viewModel,
                        onNavigateToNowPlaying = { navController.navigate(Screen.NowPlaying.route) },
                        onOpenSongMenu = { song -> playlistPickerForSong = song }
                    )
                }

                // Settings
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToAbout = { navController.navigate(Screen.About.route) }
                    )
                }

                // Now Playing
                composable(Screen.NowPlaying.route) {
                    NowPlayingScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onNavigateToQueue = { navController.navigate(Screen.Queue.route) },
                        onNavigateToEqualizer = { navController.navigate(Screen.Equalizer.route) },
                        onNavigateToLyrics = { navController.navigate(Screen.Lyrics.route) }
                    )
                }

                // Queue
                composable(Screen.Queue.route) {
                    QueueScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                // Lyrics
                composable(Screen.Lyrics.route) {
                    LyricsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                // Equalizer
                composable(Screen.Equalizer.route) {
                    EqualizerScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                // Visualizer
                composable(Screen.Visualizer.route) {
                    VisualizerScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                // About
                composable(Screen.About.route) {
                    AboutScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }

    // Add to Playlist Picker Dialog
    playlistPickerForSong?.let { song ->
        val playlists by viewModel.playlists.collectAsState()
        AlertDialog(
            onDismissRequest = { playlistPickerForSong = null },
            title = { Text("Add to Playlist") },
            text = {
                if (playlists.isEmpty()) {
                    Text("No playlists found. Create one first!")
                } else {
                    Column {
                        playlists.forEach { playlist ->
                            TextButton(
                                onClick = {
                                    viewModel.addSongToPlaylist(playlist.id, song.id)
                                    playlistPickerForSong = null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                            ) {
                                Text(playlist.name, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { playlistPickerForSong = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
